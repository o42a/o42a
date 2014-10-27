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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.object.vmt.VmtIRChain.VMT_IR_CHAIN_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.core.ir.object.ObjectIROp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.CodeDirs;


public class ObjFldConfigureFn extends Fn<ObjFldConfigureFn> {

	public static final
	ExtSignature<BoolOp, ObjFldConfigureFn> OBJ_FLD_CONFIGURE =
			customSignature("ObjFldConfigureF")
			.addPtr("vmtc", VMT_IR_CHAIN_TYPE)
			.addData("object")
			.addRelPtr("offset")
			.returnBool(c -> new ObjFldConfigureFn(c));

	private ObjFldConfigureFn(FuncCaller<ObjFldConfigureFn> caller) {
		super(caller);
	}

	public final void configure(
			CodeDirs dirs,
			VmtIRChain.Op vmtc,
			ObjectIROp object,
			RelOp offset) {
		configure(dirs.code(), vmtc, object, offset)
		.goUnless(dirs.code(), dirs.falseDir());
	}

	public final BoolOp configure(
			Code code,
			VmtIRChain.Op vmtc,
			ObjectIROp object,
			RelOp offset) {
		return invoke(
				null,
				code,
				OBJ_FLD_CONFIGURE.result(),
				vmtc,
				object.toData(null, code),
				offset);
	}

}
