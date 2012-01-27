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

import static org.o42a.core.st.impl.imperative.InlineSentence.inlineSentence;
import static org.o42a.util.Cancellation.cancelAll;
import static org.o42a.util.Cancellation.cancelUpToNull;

import java.util.List;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.value.ValueStruct;


public class InlineBlock extends InlineValue {

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

		return new InlineBlock(valueStruct, inlines);
	}

	private final InlineSentence[] sentences;

	private InlineBlock(
			ValueStruct<?, ?> valueStruct,
			InlineSentence[] sentences) {
		super(valueStruct);
		this.sentences = sentences;
	}

	public final InlineSentence get(int index) {
		return this.sentences[index];
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {
		// TODO Auto-generated method stub
		super.writeCond(dirs, host);
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		cancelAll(this.sentences);
	}

}
