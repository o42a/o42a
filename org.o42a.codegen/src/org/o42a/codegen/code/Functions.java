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

import java.util.HashMap;
import java.util.LinkedList;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


public abstract class Functions {

	private final HashMap<String, FuncPtr<?>> externals = new HashMap<>();
	private final LinkedList<Function<?>> functions = new LinkedList<>();

	private final Generator generator;

	public Functions(Generator generator) {
		this.generator = generator;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final <F extends Fn<F>> FuncPtr<F> nullPtr(
			Signature<F> signature) {
		return new NullFuncPtr<>(allocate(signature));
	}

	public final FunctionSettings newFunction() {
		return new FunctionSettings(this);
	}

	public final ExternalFunctionSettings externalFunction() {
		return new ExternalFunctionSettings(this);
	}

	public final <F extends Fn<F>> Signature<F> allocate(
			Signature<F> signature) {
		return signature.allocate(getGenerator());
	}

	public boolean write() {
		if (this.functions.isEmpty()) {
			return false;
		}
		for (;;) {

			final Function<?> function = this.functions.poll();

			if (function == null) {
				return true;
			}
			function.build();
		}
	}

	protected abstract CodeBackend codeBackend();

	protected abstract DataWriter dataWriter();

	protected abstract BeforeReturn createBeforeReturn(Function<?> function);

	protected abstract <F extends Fn<F>> void addFunction(
			ID id,
			Signature<F> signature,
			FuncPtr<F> function);

	protected <F extends Fn<F>> void addFunction(Function<F> function) {
		this.functions.add(function);
	}

	protected abstract DataAllocation<AnyOp> codeToAny(CodePtr ptr);

	final <F extends Fn<F>> FuncPtr<F> externalFunction(
			String name,
			Signature<F> signature,
			ExternalFunctionSettings settings) {

		@SuppressWarnings("unchecked")
		final FuncPtr<F> found = (FuncPtr<F>) this.externals.get(name);

		if (found != null) {
			return found;
		}

		final ID id = ID.rawId(name);
		final ExternFuncPtr<F> extern =
				new ExternFuncPtr<>(id, allocate(signature), settings);

		this.externals.put(name, extern);
		addFunction(id, signature, extern);

		return extern;
	}

}
