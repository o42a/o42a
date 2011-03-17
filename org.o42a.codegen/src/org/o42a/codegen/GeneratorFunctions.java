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
package org.o42a.codegen;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.backend.CodeCallback;


final class GeneratorFunctions extends Functions {

	GeneratorFunctions(Generator generator) {
		super(generator);
	}

	@Override
	public CodeBackend codeBackend() {
		return getGenerator().codeBackend();
	}

	@Override
	protected <F extends Func> void addFunction(
			CodeId id,
			Signature<F> signature,
			CodePtr<F> function) {
		getGenerator().addFunction(id, signature, function);
	}

	@Override
	protected CodeCallback createCodeCallback(Function<?> function) {
		return getGenerator().createCodeCallback(function);
	}

}
