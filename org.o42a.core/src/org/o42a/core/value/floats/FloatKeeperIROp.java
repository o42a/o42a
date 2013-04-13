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
package org.o42a.core.value.floats;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.Fp64recOp;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.KeeperIROp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.string.ID;


final class FloatKeeperIROp extends KeeperIROp<FloatKeeperIROp> {

	FloatKeeperIROp(StructWriter<FloatKeeperIROp> writer) {
		super(writer);
	}

	@Override
	public final FloatKeeperIRType getType() {
		return (FloatKeeperIRType) super.getType();
	}

	public final Int8recOp flags(ID id, Code code) {
		return int8(id, code, getType().flags());
	}

	public final Fp64recOp value(ID id, Code code) {
		return fp64(id, code, getType().value());
	}

	@Override
	protected void writeCond(KeeperOp<FloatKeeperIROp> keeper, CodeDirs dirs) {
		new FloatKeeperEval(keeper, this).writeCond(dirs);
	}

	@Override
	protected ValOp writeValue(KeeperOp<FloatKeeperIROp> keeper, ValDirs dirs) {
		return new FloatKeeperEval(keeper, this).writeValue(dirs);
	}

	@Override
	protected ObjectOp dereference(
			KeeperOp<FloatKeeperIROp> keeper,
			CodeDirs dirs,
			ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

}
