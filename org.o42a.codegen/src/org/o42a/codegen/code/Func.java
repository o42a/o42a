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


public abstract class Func implements PtrOp {

	private final FuncCaller caller;

	public Func(FuncCaller caller) {
		this.caller = caller;
	}

	@Override
	public void allocated(Code code, StructOp enclosing) {
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

	public final FuncCaller caller() {
		return this.caller;
	}

	@Override
	public String toString() {
		return this.caller.toString();
	}

}
