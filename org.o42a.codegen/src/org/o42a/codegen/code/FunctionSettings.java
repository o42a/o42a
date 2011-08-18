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
import org.o42a.codegen.Generator;


public final class FunctionSettings {

	private final Functions functions;

	private boolean exported;

	FunctionSettings(Functions functions) {
		this.functions = functions;
	}

	public final Generator getGenerator() {
		return this.functions.getGenerator();
	}

	public final boolean isExported() {
		return this.exported;
	}

	public final FunctionSettings export() {
		this.exported = true;
		return this;
	}

	public FunctionSettings dontExport() {
		this.exported = false;
		return this;
	}

	public final FunctionSettings set(FunctionSettings settings) {
		this.exported = settings.exported;
		return this;
	}

	public <F extends Func<F>> Function<F> create(
			CodeId id,
			Signature<F> signature) {

		final Function<F> function =
				new Function<F>(this, id, signature, null);

		this.functions.addFunction(id, signature, function.getPointer());

		return function;
	}

	public <F extends Func<F>> Function<F> create(
			CodeId id,
			Signature<F> signature,
			FunctionBuilder<F> builder) {

		final Function<F> function =
				new Function<F>(this, id, signature, builder);

		this.functions.addFunction(id, function);

		return function;
	}

}
