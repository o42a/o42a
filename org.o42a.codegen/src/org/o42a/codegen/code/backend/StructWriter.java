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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public interface StructWriter extends PtrOp {

	Type<?> getType();

	RecOp<?> field(CodeId id, Code code, Data<?> field);

	RecOp<Int8op> int8(CodeId id, Code code, Int8rec field);

	RecOp<Int16op> int16(CodeId id, Code code, Int16rec field);

	RecOp<Int32op> int32(CodeId id, Code code, Int32rec field);

	RecOp<Int64op> int64(CodeId id, Code code, Int64rec field);

	RecOp<Fp32op> fp32(CodeId id, Code code, Fp32rec field);

	RecOp<Fp64op> fp64(CodeId id, Code code, Fp64rec field);

	RecOp<AnyOp> ptr(CodeId id, Code code, AnyPtrRec field);

	RecOp<DataOp> ptr(CodeId id, Code code, DataRec field);

	<P extends StructOp> RecOp<P> ptr(CodeId id, Code code, StructRec<P> field);

	RecOp<RelOp> relPtr(CodeId id, Code code, RelPtrRec field);

	<O extends StructOp> O struct(CodeId id, Code code, Type<O> field);

	<F extends Func> FuncOp<F> func(CodeId id, Code code, FuncRec<F> field);

	DataOp toData(CodeId id, Code code);

	<O extends StructOp> O to(CodeId id, Code code, Type<O> type);

}
