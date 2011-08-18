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
package org.o42a.backend.constant.code;

import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


final class FuncCCaller<F extends Func<F>>
		extends PtrCOp<F>
		implements FuncCaller<F> {

	private final FuncCAlloc<F> allocation;

	FuncCCaller(FuncCAlloc<F> allocation, F underlying) {
		super(underlying);
		this.allocation = allocation;
	}

	public final FuncCAlloc<F> getAllocation() {
		return this.allocation;
	}

	@Override
	public final Signature<F> getSignature() {
		return getAllocation().getSignature();
	}

	@Override
	public void call(Code code, Op... args) {
		getUnderlying().caller().call(code, underlyingArgs(args));
	}

	@Override
	public Int8op callInt8(CodeId id, Code code, Op... args) {
		return null;
	}

	@Override
	public Int16op callInt16(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int32op callInt32(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int64op callInt64(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp32op callFp32(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp64op callFp64(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoolOp callBool(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnyOp callAny(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataOp callData(CodeId id, Code code, Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> S callPtr(
			CodeId id,
			Code code,
			Type<S> type,
			Op... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public F create(CodeId id, CCode<?> code, F underlying) {

		final F underlyingFunc =
				this.allocation.getUnderlyingPtr().op(id, code.getUnderlying());

		return this.allocation.getSignature().op(
				new FuncCCaller<F>(this.allocation, underlyingFunc));
	}

	private Op[] underlyingArgs(Op[] args) {
		if (args.length == 0) {
			return args;
		}

		final Op[] underlying = new Op[args.length];

		for (int i = 0; i < args.length; ++i) {
			underlying[i] = underlying(args[i]);
		}

		return underlying;
	}

}
