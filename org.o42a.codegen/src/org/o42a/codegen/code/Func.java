/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import org.o42a.util.string.ID;


public abstract class Func<F extends Func<F>> implements PtrOp<F> {

	private final FuncCaller<F> caller;

	public Func(FuncCaller<F> caller) {
		this.caller = caller;
	}

	@Override
	public final ID getId() {
		return this.caller.getId();
	}

	public final Signature<F> getSignature() {
		return this.caller.getSignature();
	}

	public final FuncCaller<F> getCaller() {
		return this.caller;
	}

	@Override
	public final void returnValue(Block code) {
		this.caller.returnValue(code);
	}

	@Override
	public final BoolOp isNull(ID id, Code code) {
		return this.caller.isNull(id, code);
	}

	@Override
	public final BoolOp eq(ID id, Code code, F other) {
		return this.caller.eq(id, code, other);
	}

	@Override
	public BoolOp ne(ID id, Code code, F other) {
		return this.caller.ne(id, code, other);
	}

	@Override
	public final AnyOp toAny(ID id, Code code) {
		return this.caller.toAny(id, code);
	}

	public final FuncCaller<F> caller() {
		return this.caller;
	}

	@Override
	public String toString() {
		return this.caller.toString();
	}

	protected final <O> O invoke(
			ID id,
			Code code,
			Return<O> ret,
			Op... args) {
		assert validSignature(ret.getSignature(), args);
		return ret.call(id, code, this.caller, args);
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
