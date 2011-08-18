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

import org.o42a.backend.constant.data.ConstBackend;
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

	public final CodeCallback getCallback() {
		return this.callback;
	}

	@Override
	public final FuncCAlloc<F> getAllocation() {
		return this.allocation;
	}

	@Override
	public Int8op int8arg(Code code, Arg<Int8op> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int16op int16arg(Code code, Arg<Int16op> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int32op int32arg(Code code, Arg<Int32op> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int64op int64arg(Code code, Arg<Int64op> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp32op fp32arg(Code code, Arg<Fp32op> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp64op fp64arg(Code code, Arg<Fp64op> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoolOp boolArg(Code code, Arg<BoolOp> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelOp relPtrArg(Code code, Arg<RelOp> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnyOp ptrArg(Code code, Arg<AnyOp> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataOp dataArg(Code code, Arg<DataOp> arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> S ptrArg(
			Code code,
			Arg<S> arg,
			Type<S> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <FF extends Func<FF>> FF funcPtrArg(
			Code code,
			Arg<FF> arg,
			Signature<FF> signature) {
		// TODO Auto-generated method stub
		return null;
	}

}
