/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.code.CFunc;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.data.func.CFAlloc;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.util.string.ID;


public final class FuncCOp<F extends Fn<F>>
		extends AtomicRecCOp<FuncOp<F>, F, FuncPtr<F>>
		implements FuncOp<F> {

	private final Signature<F> signature;

	public FuncCOp(
			OpBE<FuncOp<F>> backend,
			RecStore store,
			Signature<F> signature) {
		super(backend, store);
		this.signature = signature;
	}

	public FuncCOp(
			OpBE<FuncOp<F>> backend,
			RecStore store,
			Signature<F> signature,
			Ptr<FuncOp<F>> constant) {
		super(backend, store, constant);
		this.signature = signature;
	}

	@Override
	public final Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public final <FF extends Fn<FF>> FuncCOp<FF> toFunc(
			final ID id,
			final Code code,
			final Signature<FF> signature) {
		return new FuncCOp<>(
				new OpBE<FuncOp<FF>>(id, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected FuncOp<FF> write() {
						return backend().underlying().toFunc(
								getId(),
								part().underlying(),
								getBackend().underlying(signature));
					}
				},
				store(),
				signature);
	}

	@Override
	public FuncOp<F> create(OpBE<FuncOp<F>> backend, Ptr<FuncOp<F>> constant) {
		return new FuncCOp<>(backend, null, getSignature(), constant);
	}

	@Override
	protected F loaded(OpBE<F> backend, FuncPtr<F> constant) {
		return getSignature().op(
				new CFunc<>(backend, getSignature(), constant));
	}

	@Override
	protected F underlyingConstant(CCodePart<?> part, FuncPtr<F> constant) {

		final CFAlloc<F> alloc = (CFAlloc<F>) constant.getAllocation();

		return alloc.getUnderlyingPtr().op(null, part.underlying());
	}

}
