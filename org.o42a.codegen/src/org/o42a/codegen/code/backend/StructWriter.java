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

	Int8recOp int8(CodeId id, Code code, Int8rec field);

	Int16recOp int16(CodeId id, Code code, Int16rec field);

	Int32recOp int32(CodeId id, Code code, Int32rec field);

	Int64recOp int64(CodeId id, Code code, Int64rec field);

	Fp32recOp fp32(CodeId id, Code code, Fp32rec field);

	Fp64recOp fp64(CodeId id, Code code, Fp64rec field);

	AnyRecOp ptr(CodeId id, Code code, AnyPtrRec field);

	DataRecOp ptr(CodeId id, Code code, DataRec field);

	<S extends StructOp> StructRecOp<S> ptr(
			CodeId id,
			Code code,
			StructRec<S> field);

	RelRecOp relPtr(CodeId id, Code code, RelPtrRec field);

	<S extends StructOp> S struct(CodeId id, Code code, Type<S> field);

	<F extends Func> FuncOp<F> func(CodeId id, Code code, FuncRec<F> field);

	DataOp toData(CodeId id, Code code);

	<S extends StructOp> S to(CodeId id, Code code, Type<S> type);

}
