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

import static org.o42a.core.st.impl.declarative.DeclarativeOp.writeSentences;
import static org.o42a.core.st.impl.declarative.InlineDeclarativeSentence.inlineSentence;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public class InlineDeclarativeSentences extends InlineEval {

	public static InlineDeclarativeSentences inlineBlock(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin,
			DeclarativeSentences block) {

		final List<DeclarativeSentence> sentences = block.getSentences();
		final InlineDeclarativeSentence[] inlines =
				new InlineDeclarativeSentence[sentences.size()];
		int i = 0;

		for (DeclarativeSentence sentence : sentences) {
			inlines[i++] =
					inlineSentence(normalizer, valueStruct, origin, sentence);
		}

		return normalizer.isCancelled() ? null
			: new InlineDeclarativeSentences(block, inlines);
	}

	private final DeclarativeSentences block;
	private final InlineDeclarativeSentence[] sentences;

	private InlineDeclarativeSentences(
			DeclarativeSentences block,
			InlineDeclarativeSentence[] sentences) {
		super(null);
		this.block = block;
		this.sentences = sentences;
	}

	public final InlineDeclarativeSentence get(int index) {
		return this.sentences[index];
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {
		writeSentences(dirs, host, this.block, this);
	}

	@Override
	public String toString() {
		if (this.sentences == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('(');
		if (this.sentences.length > 0) {
			out.append(this.sentences[0]);
			for (int i = 1; i < this.sentences.length; ++i) {
				out.append(' ').append(this.sentences[i]);
			}
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
