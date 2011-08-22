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

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.func.CFunc;
import org.o42a.backend.constant.code.func.FuncCAlloc;
import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public class CFunction<F extends Func<F>>
		extends CCode<Function<F>>
		implements FuncWriter<F> {

	private final CodeCallback callback;
	private final FuncCAlloc<F> allocation;

	CFunction(
			ConstBackend backend,
			Function<F> function,
			CodeCallback callback,
			FuncCAlloc<F> allocation,
			Function<F> underlying) {
		super(backend, null, function, underlying);
		this.callback = callback;
		this.allocation = allocation;
	}

	public final CSignature<F> getUnderlyingSignature() {
		return this.allocation.getUnderlyingSignature();
	}

	public final CodeCallback getCallback() {
		return this.callback;
	}

	@Override
	public final FuncCAlloc<F> getAllocation() {
		return this.allocation;
	}

	@Override
	public final Int8cOp int8arg(Code code, Arg<Int8op> arg) {

		final CCode<?> ccode = cast(code);
		final Int8op underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new Int8cOp(ccode, underlyingValue);
	}

	@Override
	public final Int16cOp int16arg(Code code, Arg<Int16op> arg) {

		final CCode<?> ccode = cast(code);
		final Int16op underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new Int16cOp(ccode, underlyingValue);
	}

	@Override
	public final Int32cOp int32arg(Code code, Arg<Int32op> arg) {

		final CCode<?> ccode = cast(code);
		final Int32op underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new Int32cOp(ccode, underlyingValue);
	}

	@Override
	public final Int64cOp int64arg(Code code, Arg<Int64op> arg) {

		final CCode<?> ccode = cast(code);
		final Int64op underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new Int64cOp(ccode, underlyingValue);
	}

	@Override
	public final Fp32cOp fp32arg(Code code, Arg<Fp32op> arg) {

		final CCode<?> ccode = cast(code);
		final Fp32op underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new Fp32cOp(ccode, underlyingValue);
	}

	@Override
	public final Fp64cOp fp64arg(Code code, Arg<Fp64op> arg) {

		final CCode<?> ccode = cast(code);
		final Fp64op underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new Fp64cOp(ccode, underlyingValue);
	}

	@Override
	public final BoolCOp boolArg(Code code, Arg<BoolOp> arg) {

		final CCode<?> ccode = cast(code);
		final BoolOp underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new BoolCOp(ccode, underlyingValue);
	}

	@Override
	public final RelCOp relPtrArg(Code code, Arg<RelOp> arg) {

		final CCode<?> ccode = cast(code);
		final RelOp underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new RelCOp(ccode, underlyingValue);
	}

	@Override
	public final AnyCOp ptrArg(Code code, Arg<AnyOp> arg) {

		final CCode<?> ccode = cast(code);
		final AnyOp underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new AnyCOp(ccode, underlyingValue);
	}

	@Override
	public final DataCOp dataArg(Code code, Arg<DataOp> arg) {

		final CCode<?> ccode = cast(code);
		final DataOp underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return new DataCOp(ccode, underlyingValue);
	}

	@Override
	public <S extends StructOp<S>> S ptrArg(
			Code code,
			Arg<S> arg,
			Type<S> type) {

		final CCode<?> ccode = cast(code);
		final S underlyingValue =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return type.op(new CStruct<S>(ccode, underlyingValue, type));
	}

	@Override
	public <FF extends Func<FF>> FF funcPtrArg(
			Code code,
			Arg<FF> arg,
			Signature<FF> signature) {

		final CCode<?> ccode = cast(code);
		final FF underlyingFunc =
				getUnderlying().arg(ccode.getUnderlying(), underlyingArg(arg));

		return signature.op(new CFunc<FF>(ccode, underlyingFunc));
	}

	@SuppressWarnings("unchecked")
	private final <O extends Op> Arg<O> underlyingArg(Arg<O> arg) {

		final CSignature<F> underlyingSignature = getUnderlyingSignature();
		final Generator underlyingGenerator =
				underlyingSignature.getBackend().getUnderlyingGenerator();
		final Arg<?>[] underlyingArgs =
				underlyingSignature.args(underlyingGenerator);

		return (Arg<O>) underlyingArgs[arg.getIndex()];
	}

}
