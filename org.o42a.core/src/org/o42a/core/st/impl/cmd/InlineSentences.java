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
package org.o42a.core.st.impl.cmd;

import static org.o42a.core.st.impl.cmd.SentencesOp.writeSentences;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.util.fn.Cancelable;


public class InlineSentences extends InlineCmd {

	private final Sentences sentences;
	private final Scope origin;
	private final InlineSentence[] inlines;

	InlineSentences(
			Sentences sentences,
			Scope origin,
			InlineSentence[] inlines) {
		super(null);
		this.sentences = sentences;
		this.origin = origin;
		this.inlines = inlines;
	}

	public final InlineSentence get(int index) {
		return this.inlines[index];
	}

	@Override
	public void write(Control control) {
		writeSentences(control, this.origin, this.sentences, this);
	}

	@Override
	public String toString() {
		if (this.inlines == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.sentences.isParentheses() ? '(' : '{');
		if (this.inlines.length > 0) {
			out.append(this.inlines[0]);
			for (int i = 1; i < this.inlines.length; ++i) {
				out.append(' ').append(this.inlines[i]);
			}
		}
		out.append(this.sentences.isParentheses() ? ')' : '}');

		return out.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
