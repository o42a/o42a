/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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
import org.o42a.codegen.data.*;


public interface StructWriter extends PtrOp {

	Type<?> getType();

	DataOp<Int32op> int32(Code code, Int32rec field);

	DataOp<Int64op> int64(Code code, Int64rec field);

	DataOp<Fp64op> fp64(Code code, Fp64rec field);

	DataOp<AnyOp> ptr(Code code, AnyPtrRec field);

	<P extends StructOp> DataOp<P> ptr(Code code, StructPtrRec<P> field);

	DataOp<RelOp> relPtr(Code code, RelPtrRec field);

	<O extends PtrOp> O struct(Code code, Type<O> field);

	<F extends Func> CodeOp<F> func(Code code, CodeRec<F> field);

	CodeBackend backend();

}
