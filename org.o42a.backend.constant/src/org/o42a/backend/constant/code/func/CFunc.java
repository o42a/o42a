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
package org.o42a.backend.constant.code.func;

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public final class CFunc<F extends Func<F>>
		extends PtrCOp<F>
		implements FuncCaller<F> {

	public CFunc(CCode<?> code, F underlying) {
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
	public final void call(Code code, Op... args) {
		getUnderlying().caller().call(underlying(code), underlyingArgs(args));
	}

	@Override
	public final Int8cOp callInt8(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final Int8op underlyingResult = getUnderlying().caller().callInt8(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new Int8cOp(ccode, underlyingResult);
	}

	@Override
	public final Int16cOp callInt16(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final Int16op underlyingResult = getUnderlying().caller().callInt16(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new Int16cOp(ccode, underlyingResult);
	}

	@Override
	public final Int32cOp callInt32(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final Int32op underlyingResult = getUnderlying().caller().callInt32(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new Int32cOp(ccode, underlyingResult);
	}

	@Override
	public final Int64cOp callInt64(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final Int64op underlyingResult = getUnderlying().caller().callInt64(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new Int64cOp(ccode, underlyingResult);
	}

	@Override
	public final Fp32cOp callFp32(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final Fp32op underlyingResult = getUnderlying().caller().callFp32(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new Fp32cOp(ccode, underlyingResult);
	}

	@Override
	public final Fp64cOp callFp64(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final Fp64op underlyingResult = getUnderlying().caller().callFp64(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new Fp64cOp(ccode, underlyingResult);
	}

	@Override
	public final BoolCOp callBool(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingResult = getUnderlying().caller().callBool(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new BoolCOp(ccode, underlyingResult);
	}

	@Override
	public final AnyCOp callAny(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final AnyOp underlyingResult = getUnderlying().caller().callAny(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new AnyCOp(ccode, underlyingResult);
	}

	@Override
	public final DataCOp callData(CodeId id, Code code, Op... args) {

		final CCode<?> ccode = cast(code);
		final DataOp underlyingResult = getUnderlying().caller().callData(
				id,
				ccode.getUnderlying(),
				underlyingArgs(args));

		return new DataCOp(ccode, underlyingResult);
	}

	@Override
	public <S extends StructOp<S>> S callPtr(
			CodeId id,
			Code code,
			Type<S> type,
			Op... args) {

		final CCode<?> ccode = cast(code);
		final S underlyingResult = getUnderlying().caller().callPtr(
				id,
				ccode.getUnderlying(),
				getBackend().underlying(type),
				underlyingArgs(args));

		return type.op(new CStruct<S>(ccode, underlyingResult, type));
	}

	@Override
	public F create(CCode<?> code, F underlying) {
		return getSignature().op(new CFunc<F>(code, underlying));
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
