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

import static java.util.Objects.requireNonNull;
import static org.o42a.codegen.code.BeforeReturn.NOTHING_BEFORE_RETURN;
import static org.o42a.util.fn.Init.init;

import java.util.LinkedList;

import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public final class Function<F extends Fn<F>>
		extends Allocator
		implements FunctionAttributes {

	private final FunctionSettings settings;
	private final Signature<F> signature;
	private final FunctionBuilder<F> builder;
	private final FuncPtr<F> pointer;
	private final LinkedList<FunctionCompleteListener> completeListeners =
			new LinkedList<>();
	private final Init<FuncWriter<F>> writer = init(this::createWriter);
	private boolean done;

	Function(
			FunctionSettings settings,
			ID id,
			Signature<F> signature,
			FunctionBuilder<F> builder) {
		super(settings.getGenerator(), id);
		setOpNames(new OpNames.FunctionOpNames(this));
		this.settings = settings;
		this.builder = builder;
		this.signature = getGenerator().getFunctions().allocate(signature);
		this.pointer = new ConstructingFuncPtr<>(this);
		initAllocations(null);
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public final int getFunctionFlags() {
		return this.settings.getFunctionFlags();
	}

	public final FuncPtr<F> getPointer() {
		return this.pointer;
	}

	@Override
	public final Allocator getEnclosingAllocator() {
		return null;
	}

	@Override
	public boolean created() {
		return this.writer.isInitialized();
	}

	@Override
	public final boolean exists() {
		return this.writer.isInitialized() && this.writer.get().exists();
	}

	public final boolean isDone() {
		return this.done;
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
		return this.writer.get();
	}

	public void addCompleteListener(FunctionCompleteListener listener) {
		requireNonNull(listener, "Function complete listener not specified");
		this.completeListeners.add(listener);
	}

	@Override
	public void done() {
		if (this.done) {
			return;
		}
		this.done = true;
		notifyCompleteListeners();
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

	private FuncWriter<F> createWriter() {

		final Functions functions = getGenerator().getFunctions();
		final BeforeReturn beforeReturn;

		if (getGenerator().isProxied()) {
			beforeReturn = NOTHING_BEFORE_RETURN;
		} else {
			beforeReturn =
					new DisposeBeforeReturn(functions.createBeforeReturn(this));
		}

		return functions.codeBackend().addFunction(this, beforeReturn);
	}

	private void notifyCompleteListeners() {
		for (FunctionCompleteListener listener : this.completeListeners) {
			listener.functionComplete(this);
		}
	}

}
