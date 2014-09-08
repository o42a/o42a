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


public abstract class Fn<F extends Fn<F>> implements PtrOp<F> {

	public static CustomSignature customSignature(String id) {
		return Signature.customSignature(id);
	}

	public static CustomSignature customSignature(ID id) {
		return Signature.customSignature(id);
	}

	public static CustomSignature customSignature(String id, int numArgs) {
		return Signature.customSignature(id, numArgs);
	}

	public static CustomSignature customSignature(ID id, int numArgs) {
		return Signature.customSignature(id, numArgs);
	}

	private final FuncCaller<F> caller;

	public Fn(FuncCaller<F> caller) {
		this.caller = caller;
	}

	@Override
	public final ID getId() {
		return caller().getId();
	}

	public final Signature<F> getSignature() {
		return caller().getSignature();
	}

	@Override
	public final void returnValue(Block code, boolean dispose) {
		caller().returnValue(code, dispose);
	}

	@Override
	public final BoolOp isNull(ID id, Code code) {
		return caller().isNull(id, code);
	}

	@Override
	public final BoolOp eq(ID id, Code code, F other) {
		return caller().eq(id, code, other);
	}

	@Override
	public BoolOp ne(ID id, Code code, F other) {
		return caller().ne(id, code, other);
	}

	@Override
	public final AnyOp toAny(ID id, Code code) {
		return caller().toAny(id, code);
	}

	public final FuncCaller<F> caller() {
		return this.caller;
	}

	@Override
	public String toString() {
		return caller().toString();
	}

	protected final <O> O invoke(
			ID id,
			Code code,
			Return<O> ret,
			Op... args) {
		assert validSignature(ret.getSignature(), args);
		return ret.call(id, code, caller(), args);
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
