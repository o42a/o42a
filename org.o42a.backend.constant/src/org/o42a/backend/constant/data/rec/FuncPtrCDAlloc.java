/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.backend.constant.data.rec;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.rec.FuncCOp;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.RecCDAlloc;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.FuncRec;
import org.o42a.codegen.data.SubData;


public final class FuncPtrCDAlloc<F extends Func<F>>
		extends RecCDAlloc<FuncRec<F>, FuncOp<F>, FuncPtr<F>> {

	private final Signature<F> signature;

	public FuncPtrCDAlloc(
			ContainerCDAlloc<?> enclosing,
			FuncRec<F> data,
			CDAlloc<FuncOp<F>, FuncRec<F>> type,
			Signature<F> signature) {
		super(enclosing, data, type);
		this.signature = signature;
	}

	@Override
	protected FuncCOp<F> op(CCode<?> code, FuncOp<F> underlying) {
		return new FuncCOp<F>(code, underlying);
	}

	@Override
	protected FuncRec<F> allocateUnderlying(SubData<?> container, String name) {

		final CSignature<F> underlyingSignature =
				getTopLevel().getBackend().underlying(this.signature);

		return container.addFuncPtr(name, underlyingSignature);
	}

}
