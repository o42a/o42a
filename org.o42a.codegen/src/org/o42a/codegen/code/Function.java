/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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


public final class Function<F extends Func<F>>
		extends Block
		implements FunctionProperties {

	private final FunctionSettings settings;
	private final Signature<F> signature;
	private final FunctionBuilder<F> builder;
	private final FuncPtr<F> pointer;
	private FuncWriter<F> writer;
	private boolean done;

	Function(
			FunctionSettings settings,
			CodeId id,
			Signature<F> signature,
			FunctionBuilder<F> builder) {
		super(settings.getGenerator(), id);
		setOpNames(new OpNames.FunctionOpNames(this));
		this.settings = settings;
		this.builder = builder;
		this.signature = getGenerator().getFunctions().allocate(signature);
		this.pointer = new ConstructingFuncPtr<F>(this);
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public final boolean isExported() {
		return this.settings.isExported();
	}

	@Override
	public final boolean hasSideEffects() {
		return this.settings.hasSideEffects();
	}

	@Override
	public final int getFunctionFlags() {
		return this.settings.getFunctionFlags();
	}

	public final FuncPtr<F> getPointer() {
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

		final O op = arg.get(code, writer());

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

		return this.writer = functions.codeBackend().addFunction(
				this,
				functions.createCodeCallback(this));
	}

	@Override
	public void done() {
		if (this.done) {
			return;
		}
		this.done = true;
		super.done();
		if (created()) {
			writer().done();
		}
	}

	@Override
	public String toString() {
		return getSignature().toString(getId().toString());
	}

	final void build() {
		this.builder.build(this);
		done();
	}

}
