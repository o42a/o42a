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
package org.o42a.backend.constant.code.rec;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.CFunc;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.FuncOp;


public final class FuncCOp<F extends Func<F>>
		extends RecCOp<FuncOp<F>, F>
		implements FuncOp<F> {

	public FuncCOp(CCode<?> code, FuncOp<F> underlying) {
		super(code, underlying);
	}

	@Override
	public final Signature<F> getSignature() {
		return getUnderlyingSignature().getOriginal();
	}

	public final CSignature<F> getUnderlyingSignature() {
		return (CSignature<F>) getUnderlying().getSignature();
	}

	@Override
	public final <FF extends Func<FF>> FuncCOp<FF> toFunc(
			CodeId id,
			Code code,
			Signature<FF> signature) {

		final CCode<?> ccode = cast(code);
		final FuncOp<FF> underlyingFunc = getUnderlying().toFunc(
				id,
				ccode.getUnderlying(),
				getBackend().underlying(signature));

		return new FuncCOp<FF>(ccode, underlyingFunc);
	}

	@Override
	public final FuncCOp<F> create(CCode<?> code, FuncOp<F> underlying) {
		return new FuncCOp<F>(code, underlying);
	}

	@Override
	protected F loaded(CCode<?> code, F underlying) {
		return getSignature().op(new CFunc<F>(code, underlying));
	}

}
