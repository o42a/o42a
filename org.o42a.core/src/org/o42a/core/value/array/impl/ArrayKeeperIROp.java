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
package org.o42a.core.value.array.impl;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.KeeperIROp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;


final class ArrayKeeperIROp extends KeeperIROp<ArrayKeeperIROp> {

	ArrayKeeperIROp(StructWriter<ArrayKeeperIROp> writer) {
		super(writer);
	}

	@Override
	public final ArrayKeeperIRType getType() {
		return (ArrayKeeperIRType) super.getType();
	}

	public final ValType.Op value(Code code) {
		return struct(null, code, getType().value());
	}

	@Override
	protected void writeCond(KeeperOp<ArrayKeeperIROp> keeper, CodeDirs dirs) {
		new ArrayKeeperEval(keeper, this).writeCond(dirs);
	}

	@Override
	protected ValOp writeValue(KeeperOp<ArrayKeeperIROp> keeper, ValDirs dirs) {
		return new ArrayKeeperEval(keeper, this).writeValue(dirs);
	}

	@Override
	protected ObjectOp dereference(
			KeeperOp<ArrayKeeperIROp> keeper,
			CodeDirs dirs,
			ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

}
