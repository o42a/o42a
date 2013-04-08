/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import static org.o42a.core.st.impl.imperative.InlineImperativeSentence.inlineSentence;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.util.fn.Cancelable;


public class InlineImperativeBlock extends InlineCmd {

	public static InlineImperativeBlock inlineBlock(
			RootNormalizer rootNormalizer,
			Normalizer normalizer,
			Scope origin,
			ImperativeBlock block) {

		final List<ImperativeSentence> sentences = block.getSentences();
		final InlineImperativeSentence[] inlines =
				new InlineImperativeSentence[sentences.size()];
		int i = 0;

		for (ImperativeSentence sentence : sentences) {
			inlines[i++] = inlineSentence(
					rootNormalizer,
					normalizer,
					origin,
					sentence);
		}

		if (normalizer != null && normalizer.isCancelled()) {
			return null;
		}

		return new InlineImperativeBlock(block, origin, inlines);
	}

	private final ImperativeBlock block;
	private final Scope origin;
	private final InlineImperativeSentence[] sentences;

	private InlineImperativeBlock(
			ImperativeBlock block,
			Scope origin,
			InlineImperativeSentence[] sentences) {
		super(null);
		this.block = block;
		this.origin = origin;
		this.sentences = sentences;
	}

	public final InlineImperativeSentence get(int index) {
		return this.sentences[index];
	}

	@Override
	public void write(Control control) {
		writeSentences(control, this.origin, this.block, this);
	}

	@Override
	public String toString() {
		if (this.sentences == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.block.isParentheses() ? '(' : '{');
		if (this.sentences.length > 0) {
			out.append(this.sentences[0]);
			for (int i = 1; i < this.sentences.length; ++i) {
				out.append(' ').append(this.sentences[i]);
			}
		}
		out.append(this.block.isParentheses() ? ')' : '}');

		return out.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
