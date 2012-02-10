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

import java.util.HashMap;
import java.util.LinkedList;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Functions {

	private final HashMap<String, FuncPtr<?>> externals =
			new HashMap<String, FuncPtr<?>>();
	private final LinkedList<Function<?>> functions =
			new LinkedList<Function<?>>();

	private final Generator generator;

	public Functions(Generator generator) {
		this.generator = generator;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final <F extends Func<F>> FuncPtr<F> nullPtr(
			Signature<F> signature) {
		return new NullFuncPtr<F>(allocate(signature));
	}

	public final FunctionSettings newFunction() {
		return new FunctionSettings(this);
	}

	public <F extends Func<F>> FuncPtr<F> externalFunction(
			String name,
			Signature<F> signature) {

		@SuppressWarnings("unchecked")
		final FuncPtr<F> found = (FuncPtr<F>) this.externals.get(name);

		if (found != null) {
			return found;
		}

		final CodeId id = getGenerator().rawId(name);
		final ExternFuncPtr<F> extern =
				new ExternFuncPtr<F>(id, allocate(signature));

		this.externals.put(name, extern);
		addFunction(id, signature, extern);

		return extern;
	}

	public final <F extends Func<F>> Signature<F> allocate(
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

	protected abstract CodeCallback createCodeCallback(Function<?> function);

	protected abstract <F extends Func<F>> void addFunction(
			CodeId id,
			Signature<F> signature,
			FuncPtr<F> function);

	protected <F extends Func<F>> void addFunction(
			CodeId id,
			Function<F> function) {
		this.functions.add(function);
	}

}
