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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.debug.DebugEnvOp;


public final class Function<F extends Func<F>> extends Code {

	private final FunctionSettings settings;
	private final Signature<F> signature;
	private FuncWriter<F> writer;
	private FuncPtr<F> pointer;

	Function(
			FunctionSettings settings,
			CodeId id,
			Signature<F> signature) {
		super(settings.getGenerator(), id);
		this.settings = settings;
		this.signature = getGenerator().getFunctions().allocate(signature);
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public final boolean isExported() {
		return this.settings.isExported();
	}

	public final FuncPtr<F> getPointer() {
		writer();// init writer
		return this.pointer;
	}

	@Override
	public boolean created() {
		return this.writer != null;
	}

	@Override
	public final boolean exists() {
		return this.writer != null && this.writer.exists();
	}

	public final DebugEnvOp debugEnv(Code code) {

		final Arg<DebugEnvOp> debugEnv = getSignature().debugEnv();

		if (debugEnv == null) {
			assert !getSignature().isDebuggable() :
				getSignature() + " is debuggable, but does not contain"
				+ " a debug environment argument";
			return null;
		}

		return arg(code, debugEnv);
	}

	public final <O extends Op> O arg(Code code, Arg<O> arg) {
		assert getSignature() == arg.getSignature() :
			"Argument " + arg + " does not belong to " + getSignature()
			+ ". It is defined in " + arg.getSignature();

		final O op = arg.get(code, this.writer);

		assert op != null :
			"Argument " + arg + " not present in " + this;

		return op;
	}

	@Override
	public final FuncWriter<F> writer() {
		if (this.writer != null) {
			return this.writer;
		}

		final Functions functions = getGenerator().getFunctions();

		this.writer = getGenerator().getFunctions().codeBackend().addFunction(
				this,
				functions.createCodeCallback(this));
		this.pointer =
				new ConstructingFuncPtr<F>(this, this.writer.getAllocation());

		return this.writer;
	}

	@Override
	public String toString() {
		return getId().getId() + '(' + this.signature + ')';
	}

	@Override
	CodeId nestedId(CodeId name) {
		if (name != null) {
			return name;
		}
		return getGenerator().id().anonymous(++this.blockSeq);
	}

}
