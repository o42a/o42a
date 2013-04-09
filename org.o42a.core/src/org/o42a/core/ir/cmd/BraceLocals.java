/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.ir.cmd;

import java.util.IdentityHashMap;

import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.st.sentence.Local;


final class BraceLocals extends LocalsCode {

	private final LocalsCode enclosing;
	private IdentityHashMap<Local, LocalOp> locals;

	BraceLocals(LocalsCode enclosing) {
		this.enclosing = enclosing;
	}

	@Override
	public LocalOp get(Local local) {

		final LocalOp found = find(local);

		if (found != null) {
			return found;
		}

		return this.enclosing.get(local);
	}

	@Override
	public LocalOp set(CodeDirs dirs, Local local, RefOp ref) {
		if (this.locals == null) {
			this.locals = new IdentityHashMap<>();
		}

		final LocalOp op = allocate(dirs, local, ref);
		final LocalOp old = this.locals.put(local, op);

		assert old == null :
			"Local " + local + " already evaluated";

		return op;
	}

	private LocalOp find(Local local) {
		if (this.locals == null) {
			return null;
		}
		return this.locals.get(local);
	}
}
