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
package org.o42a.codegen.code.backend;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public interface FuncWriter<F extends Func> extends CodeWriter {

	CodeAllocation<F> getAllocation();

	Int32op int32arg(int index);

	Int64op int64arg(int index);

	Fp64op fp64arg(int index);

	BoolOp boolArg(Code code, int index);

	RelOp relPtrArg(Code code, int index);

	AnyOp ptrArg(Code code, int index);

	<O extends StructOp> O ptrArg(Code code, int index, Type<O> type);

}
