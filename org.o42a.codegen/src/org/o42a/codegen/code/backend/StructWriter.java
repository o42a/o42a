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
import org.o42a.codegen.data.*;


public interface StructWriter extends PtrOp {

	Type<?> getType();

	RecOp<?> field(Code code, Data<?> field);

	RecOp<Int8op> int8(Code code, Int8rec field);

	RecOp<Int16op> int16(Code code, Int16rec field);

	RecOp<Int32op> int32(Code code, Int32rec field);

	RecOp<Int64op> int64(Code code, Int64rec field);

	RecOp<Fp32op> fp32(Code code, Fp32rec field);

	RecOp<Fp64op> fp64(Code code, Fp64rec field);

	RecOp<AnyOp> ptr(Code code, AnyPtrRec field);

	RecOp<DataOp> ptr(Code code, DataRec field);

	<P extends StructOp> RecOp<P> ptr(Code code, StructRec<P> field);

	RecOp<RelOp> relPtr(Code code, RelPtrRec field);

	<O extends StructOp> O struct(Code code, Type<O> field);

	<F extends Func> FuncOp<F> func(Code code, FuncRec<F> field);

	DataOp toData(Code code);

	<O extends StructOp> O to(Code code, Type<O> type);

	CodeBackend backend();

}
