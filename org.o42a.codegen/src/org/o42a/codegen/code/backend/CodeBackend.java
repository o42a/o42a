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
package org.o42a.codegen.code.backend;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.string.ID;


public interface CodeBackend {

	<F extends Fn<F>> SignatureWriter<F> addSignature(Signature<F> signature);

	<F extends Fn<F>> FuncWriter<F> addFunction(
			Function<F> function,
			BeforeReturn beforeReturn);

	<F extends Fn<F>> FuncAllocation<F> externFunction(
			ID id,
			FuncPtr<F> pointer);

	DataAllocation<AnyOp> codeToAny(CodePtr ptr);

}
