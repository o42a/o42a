/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.data.FuncRec;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.op.ObjectFn;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.object.vmt.VmtIROp;
import org.o42a.core.ir.op.OpPresets;


public abstract class ConstructedRefFldOp<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>,
		C extends ObjectFn<C>>
				extends RefFldOp<F, T, FuncPtr<C>, FuncRec<C>, C> {

	public ConstructedRefFldOp(
			ObjOp host,
			ConstructedRefFld<F, T, C> fld,
			OpMeans<F> ptr) {
		super(host, fld, ptr);
	}

	public ConstructedRefFldOp(
			ConstructedRefFldOp<F, T, C> proto,
			OpPresets presets) {
		super(proto, presets);
	}

	@Override
	public abstract ConstructedRefFldOp<F, T, C> setPresets(OpPresets presets);

	@Override
	public ConstructedRefFld<F, T, C> fld() {
		return (ConstructedRefFld<F, T, C>) super.fld();
	}

	@Override
	protected C constructor(Code code, VmtIRChain.Op vmtc) {

		final VmtIROp vmt = vmtc.loadVmt(code, fld().getObjectIR().getVmtIR());

		return vmt.func(null, code, fld().vmtRecord().record())
				.load(null, code);
	}

}
