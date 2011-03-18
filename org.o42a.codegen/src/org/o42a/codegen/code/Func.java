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

	private final FuncCaller<?> caller;

	public Func(FuncCaller<?> caller) {
		this.caller = caller;
	}

	public final Signature<?> getSignature() {
		return this.caller.getSignature();
	}

	public final FuncCaller<?> getCaller() {
		return this.caller;
	}

	@Override
	public void allocated(Code code, StructOp enclosing) {
	}

	@Override
	public final void returnValue(Code code) {
		this.caller.returnValue(code);
	}

	@Override
	public final BoolOp isNull(Code code) {
		return this.caller.isNull(code);
	}

	@Override
	public final BoolOp eq(Code code, PtrOp other) {
		return this.caller.eq(code, other);
	}

	@Override
	public final AnyOp toAny(Code code) {
		return this.caller.toAny(code);
	}

	@Override
	public String toString() {
		return this.caller.toString();
	}

	protected final <O> O invoke(Code code, Return<O> ret, Op... args) {
		assert validSignature(ret.getSignature(), args);
		return ret.call(code, this.caller, args);
	}

	private boolean validSignature(Signature<?> signature, Op[] args) {

		final Arg<?>[] signatureArgs = signature.getArgs();

		assert signatureArgs.length == args.length :
			"Wrong number of arguments: " + args.length + ", but "
			+ signatureArgs.length + " expected by " + signature;

		for (int i = 0; i < args.length; ++i) {
			assert signatureArgs[i].compatibleWith(args[i]) :
				"Argument #" + i + " (" + args[i] + ") is not compatible with "
				+ signature;
		}

		return true;
	}

}
