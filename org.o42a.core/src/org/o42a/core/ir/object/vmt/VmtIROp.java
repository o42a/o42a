/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.vmt;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int32recOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.core.ir.object.vmt.VmtIR.VmtIRStruct;


public class VmtIROp extends StructOp<VmtIROp> {

	VmtIROp(StructWriter<VmtIROp> writer) {
		super(writer);
	}

	@Override
	public final VmtIRStruct getType() {
		return (VmtIRStruct) super.getType();
	}

	public final Int32recOp size(Code code) {
		return int32(null, code, getType().size());
	}

	public final BoolOp compatible(Code code) {

		final int expectedSize = getType().layout(code.getGenerator()).size();

		return size(code)
				.load(null, code)
				.ge(null, code, code.int32(expectedSize));
	}

}
