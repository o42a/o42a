/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public abstract class Func implements PtrOp {

	private final FuncCaller caller;

	public Func(FuncCaller caller) {
		this.caller = caller;
	}

	@Override
	public void allocated(Code code, StructOp enclosing, boolean stack) {
	}

	@Override
	public final void returnValue(Code code) {
		caller().returnValue(code);
	}

	@Override
	public final BoolOp isNull(Code code) {
		return caller().isNull(code);
	}

	@Override
	public final BoolOp eq(Code code, PtrOp other) {
		return caller().eq(code, other);
	}

	@Override
	public final AnyOp toAny(Code code) {
		return caller().toAny(code);
	}

	@Override
	public DataOp<AnyOp> toPtr(Code code) {
		return caller().toPtr(code);
	}

	@Override
	public final DataOp<Int32op> toInt32(Code code) {
		return caller().toInt32(code);
	}

	@Override
	public final DataOp<Int64op> toInt64(Code code) {
		return caller().toInt64(code);
	}

	@Override
	public final DataOp<Fp64op> toFp64(Code code) {
		return caller().toFp64(code);
	}

	@Override
	public final DataOp<RelOp> toRel(Code code) {
		return caller().toRel(code);
	}

	@Override
	public final <F extends Func> CodeOp<F> toFunc(
			Code code,
			Signature<F> signature) {
		return caller().toFunc(code, signature);
	}

	@Override
	public final <O extends StructOp> O to(Code code, Type<O> type) {
		return caller().to(code, type);
	}

	public final FuncCaller caller() {
		return this.caller;
	}

	@Override
	public String toString() {
		return this.caller.toString();
	}

}
