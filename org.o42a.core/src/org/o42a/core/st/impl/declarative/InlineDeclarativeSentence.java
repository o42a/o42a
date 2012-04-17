/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.st.impl.declarative.InlineDefiners.inlineDefiners;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.core.value.ValueStruct;


final class InlineDeclarativeSentence {

	static InlineDeclarativeSentence inlineSentence(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin,
			DeclarativeSentence sentence) {

		final DeclarativeSentence prereq = sentence.getPrerequisite();
		final InlineDeclarativeSentence inlinePrereq;

		if (prereq == null) {
			inlinePrereq = null;
		} else {
			inlinePrereq =
					inlineSentence(normalizer, valueStruct, origin, prereq);
		}

		final List<Declaratives> alts = sentence.getAlternatives();
		final InlineDefiners[] inlineAlts = new InlineDefiners[alts.size()];
		int i = 0;

		for (Declaratives alt : alts) {
			inlineAlts[i++] =
					inlineDefiners(normalizer, valueStruct, origin, alt);
		}

		return normalizer.isCancelled() ? null
			: new InlineDeclarativeSentence(sentence, inlinePrereq, inlineAlts);
	}

	private final DeclarativeSentence sentence;
	private final InlineDeclarativeSentence prerequisite;
	private final InlineDefiners[] alts;

	private InlineDeclarativeSentence(
			DeclarativeSentence sentence,
			InlineDeclarativeSentence prerequisite,
			InlineDefiners[] alts) {
		this.sentence = sentence;
		this.prerequisite = prerequisite;
		this.alts = alts;
	}

	public final InlineDeclarativeSentence getPrerequisite() {
		return this.prerequisite;
	}

	public final InlineDefiners get(int index) {
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

		if (this.sentence.isIssue()) {
			out.append('?');
		} else if (this.sentence.isClaim()) {
			out.append('!');
		} else {
			out.append('.');
		}

		return out.toString();
	}

}
