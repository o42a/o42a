/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.object;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;


final class ObjFldTargetHolder extends ObjHolder {

	private final BoolOp store;
	private final ObjHolder temp;
	private final ObjHolder trap;

	ObjFldTargetHolder(Allocator allocator, BoolOp store) {
		this.store = store;
		this.temp = tempObjHolder(allocator);
		this.trap = objTrap();
	}

	@Override
	protected void setObject(Block code, ObjectOp object) {
		// Trap set does nothing, so just ignore it.

		final Block set = code.addBlock("set");

		this.store.go(code, set.head());
		if (set.exists()) {
			this.temp.set(set, object);
			set.go(code.tail());
		}
	}

	@Override
	protected void holdObject(Block code, ObjectOp object) {
		// Temporary hold does nothing, so just ignore it.

		final Block trap = code.addBlock("trap");

		this.store.goUnless(code, trap.head());
		if (trap.exists()) {
			this.trap.hold(trap, object);
			trap.go(code.tail());
		}
	}

	@Override
	protected void holdVolatileObject(Block code, ObjectOp object) {

		final CondBlock hold = this.store.branch(code, "hold", "trap");
		final Block trap = hold.otherwise();

		if (hold.exists()) {
			this.temp.hold(hold, object);
			hold.go(code.tail());
		}
		if (trap.exists()) {
			this.trap.hold(trap, object);
			trap.go(code.tail());
		}
	}

}
