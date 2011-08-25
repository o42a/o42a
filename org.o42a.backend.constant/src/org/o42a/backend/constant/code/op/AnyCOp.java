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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.rec.*;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Type;


public final class AnyCOp extends PtrCOp<AnyOp, Ptr<AnyOp>> implements AnyOp {

	public AnyCOp(CCode<?> code, AnyOp underlying, Ptr<AnyOp> constant) {
		super(code, underlying, constant);
	}

	@Override
	public final AnyRecCOp toPtr(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final AnyRecOp underlyingRec =
				getUnderlying().toPtr(id, ccode.getUnderlying());

		return new AnyRecCOp(ccode, underlyingRec, null);
	}

	@Override
	public final Int8recCOp toInt8(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int8recOp underlyingRec =
				getUnderlying().toInt8(id, ccode.getUnderlying());

		return new Int8recCOp(ccode, underlyingRec, null);
	}

	@Override
	public final Int16recCOp toInt16(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int16recOp underlyingRec =
				getUnderlying().toInt16(id, ccode.getUnderlying());

		return new Int16recCOp(ccode, underlyingRec, null);
	}

	@Override
	public final Int32recCOp toInt32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int32recOp underlyingRec =
				getUnderlying().toInt32(id, ccode.getUnderlying());

		return new Int32recCOp(ccode, underlyingRec, null);
	}

	@Override
	public final Int64recCOp toInt64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Int64recOp underlyingRec =
				getUnderlying().toInt64(id, ccode.getUnderlying());

		return new Int64recCOp(ccode, underlyingRec, null);
	}

	@Override
	public final Fp32recCOp toFp32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Fp32recOp underlyingRec =
				getUnderlying().toFp32(id, ccode.getUnderlying());

		return new Fp32recCOp(ccode, underlyingRec, null);
	}

	@Override
	public final Fp64recOp toFp64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final Fp64recOp underlyingRec =
				getUnderlying().toFp64(id, ccode.getUnderlying());

		return new Fp64recCOp(ccode, underlyingRec, null);
	}

	@Override
	public final RelRecCOp toRel(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final RelRecOp underlyingRec =
				getUnderlying().toRel(id, ccode.getUnderlying());

		return new RelRecCOp(ccode, underlyingRec, null);
	}

	@Override
	public DataCOp toData(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final DataOp underlyingData =
				getUnderlying().toData(id, ccode.getUnderlying());

		return new DataCOp(ccode, underlyingData, null);
	}

	@Override
	public AnyCOp toAny(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);
		final AnyOp underlying = getUnderlying();
		final AnyOp underlyingAny =
				underlying.toAny(id, ccode.getUnderlying());

		if (underlying == underlyingAny) {
			return this;
		}

		return new AnyCOp(ccode, underlyingAny, getConstant());
	}

	@Override
	public final <F extends Func<F>> FuncCOp<F> toFunc(
			CodeId id,
			Code code,
			Signature<F> signature) {

		final CCode<?> ccode = cast(code);
		final FuncOp<F> underlyingFunc = getUnderlying().toFunc(
				id,
				ccode.getUnderlying(),
				getBackend().underlying(signature));

		return new FuncCOp<F>(ccode, underlyingFunc, null);
	}

	@Override
	public <S extends StructOp<S>> S to(CodeId id, Code code, Type<S> type) {

		final CCode<?> ccode = cast(code);
		final S underlyingStruct = getUnderlying().to(
				id,
				ccode.getUnderlying(),
				getBackend().underlying(type));

		return type.op(new CStruct<S>(ccode, underlyingStruct, type, null));
	}

	@Override
	public AnyCOp create(CCode<?> code, AnyOp underlying, Ptr<AnyOp> constant) {
		return new AnyCOp(code, underlying, constant);
	}

}
