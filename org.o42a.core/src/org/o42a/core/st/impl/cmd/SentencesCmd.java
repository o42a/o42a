/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;


final class SentencesCmd implements Cmd {

	private final Sentences sentences;
	private final Scope origin;

	SentencesCmd(Sentences sentences, Scope origin) {
		this.sentences = sentences;
		this.origin = origin;
	}

	@Override
	public void write(Control control) {
		writeSentences(control, this.origin, this.sentences, null);
	}

	@Override
	public String toString() {
		if (this.sentences == null) {
			return super.toString();
		}
		return this.sentences.toString();
	}

}
