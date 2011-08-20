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

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.debug.Debug;
import org.o42a.util.use.UseCaseInfo;
import org.o42a.util.use.UseFlag;
import org.o42a.util.use.User;


public abstract class Generator implements UseCaseInfo {

	private final String id;
	private final GeneratorFunctions functions;
	private final GeneratorGlobals globals;
	private boolean proxied;

	Generator(String id) {
		assert id != null :
			"Generator identifier not specified";
		this.id = id;
		this.functions = new GeneratorFunctions(this);
		this.globals = new GeneratorGlobals(this);
	}

	public final String getId() {
		return this.id;
	}

	public final boolean isProxied() {
		return this.proxied;
	}

	@Override
	public final User toUser() {
		return toUseCase();
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

	public abstract Debug getDebug();

	public final boolean isDebug() {
		return getDebug().isDebug();
	}

	public final void setDebug(boolean debug) {
		getDebug().setDebug(debug);
	}

	public abstract boolean isUsesAnalysed();

	public abstract void setUsesAnalysed(boolean usesAnalysed);

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
		return getFunctions().newFunction();
	}

	public final <F extends Func<F>> FuncPtr<F> externalFunction(
			String name,
			Signature<F> signature) {
		return getFunctions().externalFunction(name, signature);
	}

	public final GlobalSettings newGlobal() {
		return getGlobals().newGlobal();
	}

	public final Ptr<AnyOp> addBinary(
			CodeId id,
			boolean isContant,
			byte[] data) {
		return getGlobals().addBinary(id, isContant, data);
	}

	public final Ptr<AnyOp> addBinary(
			CodeId id,
			boolean isConstant,
			byte[] data,
			int start,
			int end) {
		return getGlobals().addBinary(id, isConstant, data, start, end);
	}

	public void write() {
		for (;;) {

			final boolean hadGlobals = getGlobals().write();
			final boolean hadFunctions = getFunctions().write();

			if (!hadGlobals && !hadFunctions) {
				return;
			}
		}
	}

	public abstract void close();

	protected abstract CodeBackend codeBackend();

	protected CodeCallback createCodeCallback(Function<?> function) {
		return getDebug().createCodeCallback(function);
	}

	protected abstract DataAllocator dataAllocator();

	protected abstract DataWriter dataWriter();

	protected <F extends Func<F>> void addFunction(
			CodeId id,
			FuncPtr<F> functionPtr) {
		getDebug().addFunction(id, functionPtr);
	}

	protected void registerType(SubData<?> type) {
		getDebug().registerType(type);
	}

	protected void addType(SubData<?> type) {
	}

	protected void addGlobal(SubData<?> global) {
	}

	final void proxied() {
		this.proxied = true;
	}

}
