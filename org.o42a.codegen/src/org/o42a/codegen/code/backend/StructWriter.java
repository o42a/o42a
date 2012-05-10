/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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


public interface StructWriter<S extends StructOp<S>>
		extends DumpablePtrOp<S> {

	Type<S> getType();

	Int8recOp int8(CodeId id, Code code, Int8rec field);

	Int16recOp int16(CodeId id, Code code, Int16rec field);

	Int32recOp int32(CodeId id, Code code, Int32rec field);

	Int64recOp int64(CodeId id, Code code, Int64rec field);

	Fp32recOp fp32(CodeId id, Code code, Fp32rec field);

	Fp64recOp fp64(CodeId id, Code code, Fp64rec field);

	AnyRecOp ptr(CodeId id, Code code, AnyRec field);

	DataRecOp ptr(CodeId id, Code code, DataRec field);

	<SS extends StructOp<SS>> StructRecOp<SS> ptr(
			CodeId id,
			Code code,
			StructRec<SS> field);

	RelRecOp relPtr(CodeId id, Code code, RelRec field);

	<SS extends StructOp<SS>> SS struct(CodeId id, Code code, Type<SS> field);

	<F extends Func<F>> FuncOp<F> func(CodeId id, Code code, FuncRec<F> field);

	<SS extends StructOp<SS>> SS to(CodeId id, Code code, Type<SS> type);

}
