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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.st.impl.imperative.InlineCommands.inlineCommands;
import static org.o42a.util.Cancellation.cancelAll;
import static org.o42a.util.Cancellation.cancelUpToNull;

import java.util.List;

import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.Cancelable;


public final class InlineSentence implements Cancelable {

	public static InlineSentence inlineSentence(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			ImperativeSentence sentence) {

		final ImperativeSentence prereq = sentence.getPrerequisite();
		final InlineSentence inlinePrereq;

		if (prereq == null) {
			inlinePrereq = null;
		} else {
			inlinePrereq = inlineSentence(normalizer, valueStruct, prereq);
			if (inlinePrereq == null) {
				return null;
			}
		}

		final List<Imperatives> alts = sentence.getAlternatives();
		final InlineCommands[] inlineAlts = new InlineCommands[alts.size()];
		int i = 0;

		for (Imperatives alt : alts) {

			final InlineCommands inlineAlt =
					inlineCommands(normalizer, valueStruct, alt);

			if (inlineAlt == null) {
				if (inlinePrereq != null) {
					inlinePrereq.cancel();
				}
				cancelUpToNull(inlineAlts);
				return null;
			}

			inlineAlts[i++] = inlineAlt;
		}

		return new InlineSentence(sentence, inlinePrereq, inlineAlts);
	}

	private final ImperativeSentence sentence;
	private final InlineSentence prerequisite;
	private final InlineCommands[] alts;

	private InlineSentence(
			ImperativeSentence sentence,
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
	public void cancel() {
		if (this.prerequisite != null) {
			this.prerequisite.cancel();
		}
		cancelAll(this.alts);
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
