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

import static org.o42a.core.ir.object.op.ObjectDataFunc.OBJECT_DATA;

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectOp;


final class ObjTrap extends ObjHolder {

	static final ObjTrap OBJ_TRAP = new ObjTrap();

	private ObjTrap() {
	}

	@Override
	public ObjHolder toVolatile() {
		return this;
	}

	@Override
	public String toString() {
		return "ObjTrap";
	}

	@Override
	protected void setObject(Block code, ObjectOp object) {
	}

	@Override
	protected void holdObject(Block code, ObjectOp object) {
		code.getGenerator()
		.externalFunction()
		.link("o42a_obj_use", OBJECT_DATA)
		.op(null, code)
		.call(code, object.objectType(code).ptr().data(code));
	}

	@Override
	protected void holdVolatileObject(Block code, ObjectOp object) {
		holdObject(code, object);
	}

}
