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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.st.impl.imperative.ImperativeOp.writeSentences;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.st.sentence.ImperativeBlock;


public final class ImperativeBlockCmd implements Cmd {

	private final ImperativeBlock block;
	private final Scope origin;

	public ImperativeBlockCmd(ImperativeBlock block, Scope origin) {
		this.block = block;
		this.origin = origin;
	}

	@Override
	public void write(Control control) {
		writeSentences(control, this.origin, this.block, null);
	}

	@Override
	public String toString() {
		if (this.block == null) {
			return super.toString();
		}
		return this.block.toString();
	}

}
