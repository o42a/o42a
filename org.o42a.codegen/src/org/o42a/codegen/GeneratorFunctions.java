/*
    Compiler Code Generator
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


final class GeneratorFunctions extends Functions {

	GeneratorFunctions(Generator generator) {
		super(generator);
	}

	@Override
	protected CodeBackend codeBackend() {
		return getGenerator().codeBackend();
	}

	@Override
	protected DataWriter dataWriter() {
		return getGenerator().dataWriter();
	}

	@Override
	protected BeforeReturn createBeforeReturn(Function<?> function) {
		return getGenerator().createBeforeReturn(function);
	}

	@Override
	protected <F extends Func<F>> void addFunction(
			ID id,
			Signature<F> signature,
			FuncPtr<F> function) {
		getGenerator().addFunction(id, function);
	}

	@Override
	public DataAllocation<AnyOp> codeToAny(CodePtr ptr) {
		return getGenerator().codeBackend().codeToAny(ptr);
	}

	@Override
	protected <F extends Func<F>> void addFunction(Function<F> function) {
		getGenerator().addFunction(function.getId(), function.getPointer());
		super.addFunction(function);
	}

}
