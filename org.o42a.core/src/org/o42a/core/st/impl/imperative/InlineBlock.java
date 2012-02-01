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

import static org.o42a.core.st.impl.imperative.ImperativeOp.writeSentences;
import static org.o42a.core.st.impl.imperative.InlineSentence.inlineSentence;
import static org.o42a.util.Cancellation.cancelAll;
import static org.o42a.util.Cancellation.cancelUpToNull;

import java.util.List;

import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.InlineCommand;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.value.ValueStruct;


public class InlineBlock implements InlineCommand {

	public static InlineBlock inlineBlock(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			ImperativeBlock block) {

		final List<ImperativeSentence> sentences = block.getSentences();
		final InlineSentence[] inlines = new InlineSentence[sentences.size()];
		int i = 0;

		for (ImperativeSentence sentence : sentences) {

			final InlineSentence inline =
					inlineSentence(normalizer, valueStruct, sentence);

			if (inline == null) {
				cancelUpToNull(inlines);
				return null;
			}

			inlines[i++] = inline;
		}

		return new InlineBlock(block, inlines);
	}

	private final ImperativeBlock block;
	private final InlineSentence[] sentences;

	private InlineBlock(
			ImperativeBlock block,
			InlineSentence[] sentences) {
		this.block = block;
		this.sentences = sentences;
	}

	public final InlineSentence get(int index) {
		return this.sentences[index];
	}

	@Override
	public void writeCond(Control control) {
		writeSentences(control, null, this.block, this);
	}

	@Override
	public void writeValue(Control control, ValOp result) {
		writeSentences(control, result, this.block, this);
	}

	@Override
	public void cancel() {
		cancelAll(this.sentences);
	}

	@Override
	public String toString() {
		if (this.sentences == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('{');
		if (this.sentences.length > 0) {
			out.append(this.sentences[0]);
			for (int i = 1; i < this.sentences.length; ++i) {
				out.append(' ').append(this.sentences[i]);
			}
		}
		out.append('}');

		return out.toString();
	}
}
