/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.core.st.impl.cmd;

import static org.o42a.core.st.impl.cmd.InlineCommands.inlineCommands;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.st.sentence.Statements;


final class InlineSentence {

	static InlineSentence inlineSentence(
			RootNormalizer rootNormalizer,
			Normalizer normalizer,
			Scope origin,
			Sentence<?> sentence) {

		final Sentence<?> prereq = sentence.getPrerequisite();
		final InlineSentence inlinePrereq;

		if (prereq == null) {
			inlinePrereq = null;
		} else {
			inlinePrereq = inlineSentence(
					rootNormalizer,
					normalizer,
					origin,
					prereq);
		}

		final List<? extends Statements<?>> alts =
				sentence.getAlternatives();
		final InlineCommands[] inlineAlts = new InlineCommands[alts.size()];
		int i = 0;

		for (Statements<?> alt : alts) {
			inlineAlts[i++] = inlineCommands(
					rootNormalizer,
					normalizer,
					origin,
					alt);
		}

		if (normalizer != null && normalizer.isCancelled()) {
			return null;
		}

		return new InlineSentence(sentence, inlinePrereq, inlineAlts);
	}

	private final Sentence<?> sentence;
	private final InlineSentence prerequisite;
	private final InlineCommands[] alts;

	private InlineSentence(
			Sentence<?> sentence,
			InlineSentence prerequisite,
			InlineCommands[] alts) {
		this.sentence = sentence;
		this.prerequisite = prerequisite;
		this.alts = alts;
	}

	public final InlineSentence getPrerequisite() {
		return this.prerequisite;
	}

	public final InlineCommands get(int index) {
		return this.alts[index];
	}

	@Override
	public String toString() {
		if (this.alts == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		if (this.prerequisite != null) {
			out.append(this.prerequisite).append(' ');
		}
		if (this.alts.length > 0) {
			out.append(this.alts[0]);
			for (int i = 1; i < this.alts.length; ++i) {
				out.append("; ").append(this.alts[i]);
			}
		}

		out.append(this.sentence.getKind().getSign());

		return out.toString();
	}

}
