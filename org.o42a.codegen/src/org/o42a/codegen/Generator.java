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
package org.o42a.codegen;

import static org.o42a.codegen.CodeIdFactory.DEFAULT_CODE_ID_FACTORY;
import static org.o42a.codegen.code.backend.CodeCallback.NOOP_CODE_CALLBACK;
import static org.o42a.codegen.debug.Debug.DEBUG_CODE_CALLBACK;
import static org.o42a.util.use.User.useCase;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.debug.Debug;
import org.o42a.util.use.*;


public abstract class Generator implements UseCaseInfo {

	private final String id;
	private final UseCase useCase;
	private final GeneratorFunctions functions;
	private final GeneratorGlobals globals;
	private final Debug debug;
	private boolean usesAnalysed = true;

	public Generator(String id) {
		if (id == null) {
			throw new NullPointerException(
					"Generator identifier not specified");
		}
		this.id = id;
		this.useCase = useCase(id);
		this.functions = new GeneratorFunctions(this);
		this.globals = new GeneratorGlobals(this);
		this.debug = new Debug(this);
	}

	public final String getId() {
		return this.id;
	}

	public final UseCase getUseCase() {
		return this.useCase;
	}

	@Override
	public final User toUser() {
		return this.useCase;
	}

	@Override
	public final UseCase toUseCase() {
		return this.useCase;
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		return toUser().getUseBy(useCase);
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	public CodeIdFactory getCodeIdFactory() {
		return DEFAULT_CODE_ID_FACTORY;
	}

	public final Functions getFunctions() {
		return this.functions;
	}

	public final Globals getGlobals() {
		return this.globals;
	}

	public final Debug getDebug() {
		return this.debug;
	}

	public final boolean isDebug() {
		return this.debug.isDebug();
	}

	public final void setDebug(boolean debug) {
		this.debug.setDebug(debug);
	}

	public final boolean isUsesAnalysed() {
		return this.usesAnalysed;
	}

	public final void setUsesAnalysed(boolean usesAnalysed) {
		this.usesAnalysed = usesAnalysed;
	}

	public final CodeId id() {
		return getCodeIdFactory().id();
	}

	public final CodeId topId() {
		return getCodeIdFactory().topId();
	}

	public final CodeId id(String name) {
		return getCodeIdFactory().id(name);
	}

	public final CodeId rawId(String id) {
		return getCodeIdFactory().rawId(id);
	}

	public final FunctionSettings newFunction() {
		return this.functions.newFunction();
	}

	public final <F extends Func<F>> FuncPtr<F> externalFunction(
			String name,
			Signature<F> signature) {
		return this.functions.externalFunction(name, signature);
	}

	public final GlobalSettings newGlobal() {
		return this.globals.newGlobal();
	}

	public final Ptr<AnyOp> addBinary(
			CodeId id,
			boolean isContant,
			byte[] data) {
		return this.globals.addBinary(id, isContant, data);
	}

	public final Ptr<AnyOp> addBinary(
			CodeId id,
			boolean isConstant,
			byte[] data,
			int start,
			int end) {
		return this.globals.addBinary(id, isConstant, data, start, end);
	}

	public final void write() {

		final CodeBackend coder = codeBackend();

		try {
			writeData();
		} finally {
			coder.done();
		}
	}

	public abstract void close();

	protected abstract CodeBackend codeBackend();

	protected CodeCallback createCodeCallback(Function<?> function) {
		if (isDebug() && function.getSignature().isDebuggable()) {
			return DEBUG_CODE_CALLBACK;
		}
		return NOOP_CODE_CALLBACK;
	}

	protected abstract DataAllocator dataAllocator();

	protected abstract DataWriter dataWriter();

	protected <F extends Func<F>> void addFunction(
			CodeId id,
			FuncPtr<F> functionPtr) {
		this.debug.addFunction(id, functionPtr);
	}

	protected void registerType(SubData<?> type) {
		this.debug.registerType(type);
	}

	protected void addType(SubData<?> type) {
	}

	protected void addGlobal(SubData<?> global) {
	}

	protected void writeData() {
		this.globals.write();
	}

}
