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
package org.o42a.core.ir;

import java.util.IdentityHashMap;

import org.o42a.core.ir.op.RefOp;
import org.o42a.core.st.sentence.Local;


public final class LocalsCode {

	private IdentityHashMap<Local, RefOp> locals;

	LocalsCode() {
	}

	public final RefOp get(Local local) {

		final RefOp op = this.locals != null ? this.locals.get(local) : null;

		assert op != null :
			"Local `" + local + "` did not evaluated yet";

		return op;
	}

	public final void set(Local local, RefOp op) {
		if (this.locals == null) {
			this.locals = new IdentityHashMap<>();
		}

		final RefOp old = this.locals.put(local, op);

		assert old == null :
			"Local " + local + " already evaluated";
	}

}