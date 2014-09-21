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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.object.vmt.VmtIR;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.object.vmt.VmtIROp;
import org.o42a.core.ir.op.CodeDirs;


public class VmtChainAllocFn extends Fn<VmtChainAllocFn> {

	public static ExtSignature<VmtIRChain.Op, VmtChainAllocFn> VMT_CHAIN_ALLOC =
			customSignature("VmtChainAllocF", 2)
			.addData("vmt")
			.addPtr("prev", VMT_IR_CHAIN_TYPE)
			.returnPtr(VMT_IR_CHAIN_TYPE, c -> new VmtChainAllocFn(c));

	private VmtChainAllocFn(FuncCaller<VmtChainAllocFn> caller) {
		super(caller);
	}

	public final VmtIRChain.Op allocate(
			CodeDirs dirs,
			VmtIR vmtIR,
			VmtIRChain.Op prev) {
		return allocate(
				dirs,
				vmtIR.pointer(dirs.getGenerator()).op(null, dirs.code()),
				prev);
	}

	public final VmtIRChain.Op allocate(
			CodeDirs dirs,
			VmtIROp vmt,
			VmtIRChain.Op prev) {

		final Block code = dirs.code();
		final VmtIRChain.Op vmtc = invoke(
				null,
				code,
				VMT_CHAIN_ALLOC.result(),
				vmt.toData(null, code),
				prev);

		vmtc.isNull(null, code).go(code, dirs.falseDir());

		return vmtc;
	}

}
