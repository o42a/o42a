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
package org.o42a.core.ir.object.op;

import static org.o42a.core.ir.object.impl.VariableUseFn.VARIABLE_USE;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.object.ObjectOp;


public abstract class ObjHolder {

	public static ObjHolder tempObjHolder(Allocator allocator) {
		return new TempObjHolder(allocator);
	}

	public static DataOp useVar(Code code, DataRecOp var) {
		return code.getGenerator()
				.externalFunction()
				.link("o42a_obj_use_mutable", VARIABLE_USE)
				.op(null, code)
				.use(code, var);
	}

	public static ObjHolder objTrap() {
		return ObjTrap.OBJ_TRAP;
	}

	public static ObjHolder noObjHolder() {
		return NoObjHolder.NO_OBJ_HOLDER;
	}

	public final <O extends ObjectOp> O set(Block code, O object) {
		if (!holdableObject(object)) {
			return object;
		}
		setObject(code, object);
		return object;
	}

	public final <O extends ObjectOp> O hold(Block code, O object) {
		if (!holdableObject(object)) {
			return object;
		}
		holdObject(code, object);
		return object;
	}

	public final <O extends ObjectOp> O holdVolatile(Block code, O object) {
		if (!holdableObject(object)) {
			return object;
		}
		holdVolatileObject(code, object);
		return object;
	}

	public ObjHolder toVolatile() {
		return new VolatileObjHolder(this);
	}

	protected abstract void setObject(Block code, ObjectOp object);

	protected abstract void holdObject(Block code, ObjectOp object);

	protected abstract void holdVolatileObject(Block code, ObjectOp object);

	private final boolean holdableObject(ObjectOp object) {
		return !object.isStackAllocated() && !object.getPrecision().isExact();
	}

}
