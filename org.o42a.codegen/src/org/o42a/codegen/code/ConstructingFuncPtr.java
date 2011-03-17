/*
    Compiler Code Generator
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.codegen.code.backend.CodeAllocation;


final class ConstructingFuncPtr<F extends Func> extends FuncPtr<F> {

	private final Function<F> function;

	ConstructingFuncPtr(Function<F> function, CodeAllocation<F> allocation) {
		super(function.getSignature(), allocation);
		this.function = function;
	}

	@Override
	public Function<F> getFunction() {
		return this.function;
	}

	@Override
	public String toString() {
		return "&" + this.function;
	}

}
