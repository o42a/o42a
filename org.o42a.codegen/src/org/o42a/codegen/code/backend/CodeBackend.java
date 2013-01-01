/*
    Compiler Code Generator
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.string.ID;


public interface CodeBackend {

	<F extends Func<F>> SignatureWriter<F> addSignature(Signature<F> signature);

	<F extends Func<F>> FuncWriter<F> addFunction(
			Function<F> function,
			BeforeReturn beforeReturn);

	<F extends Func<F>> FuncAllocation<F> externFunction(
			ID id,
			FuncPtr<F> pointer);

}
