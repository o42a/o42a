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
package org.o42a.codegen;

import java.util.HashMap;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.debug.Debug;
import org.o42a.util.string.ID;
import org.o42a.util.string.NameEncoder;


public abstract class Generator {

	private final GeneratorFunctions functions;
	private final GeneratorGlobals globals;
	private final HashMap<Class<?>, Object> features = new HashMap<>();
	private boolean proxied;

	Generator() {
		this.functions = new GeneratorFunctions(this);
		this.globals = new GeneratorGlobals(this);
	}

	public final boolean isProxied() {
		return this.proxied;
	}

	public abstract Analyzer getAnalyzer();

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

	public final boolean is(Generator other) {
		return this == other;
	}

	@SuppressWarnings("unchecked")
	public final <T> T getFeature(Class<? extends T> featureType) {
		return (T) this.features.get(featureType);
	}

	@SuppressWarnings("unchecked")
	public final <T> T setFeature(Class<? extends T> featureType, T feature) {
		return (T) this.features.put(featureType, feature);
	}

	public final FunctionSettings newFunction() {
		return getFunctions().newFunction();
	}

	public final ExternalFunctionSettings externalFunction() {
		return getFunctions().externalFunction();
	}

	public final GlobalSettings newGlobal() {
		return getGlobals().newGlobal();
	}

	public final ExternalGlobalSettings externalGlobal() {
		return getGlobals().externalGlobal();
	}

	public final Ptr<AnyOp> addBinary(ID id, boolean isContant, byte[] data) {
		return getGlobals().addBinary(id, isContant, data);
	}

	public final Ptr<AnyOp> addBinary(
			ID id,
			boolean isConstant,
			byte[] data,
			int start,
			int end) {
		return getGlobals().addBinary(id, isConstant, data, start, end);
	}

	public abstract NameEncoder nameEncoder();

	public void write() {
		getDebug().write();
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

	protected BeforeReturn createBeforeReturn(Function<?> function) {
		return getDebug().createBeforeReturn(function);
	}

	protected abstract DataAllocator dataAllocator();

	protected abstract DataWriter dataWriter();

	protected <F extends Fn<F>> void addFunction(
			ID id,
			FuncPtr<F> functionPtr) {
		getDebug().addFunction(id, functionPtr);
	}

	protected void registerType(SubData<?> type) {
		getDebug().registerType(type);
	}

	final void proxied() {
		this.proxied = true;
	}

}
