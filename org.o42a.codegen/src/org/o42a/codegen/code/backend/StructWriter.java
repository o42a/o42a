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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


public interface StructWriter<S extends StructOp<S>>
		extends DumpablePtrOp<S> {

	Type<S> getType();

	Int8recOp int8(ID id, Code code, Int8rec field);

	Int16recOp int16(ID id, Code code, Int16rec field);

	Int32recOp int32(ID id, Code code, Int32rec field);

	Int64recOp int64(ID id, Code code, Int64rec field);

	Fp32recOp fp32(ID id, Code code, Fp32rec field);

	Fp64recOp fp64(ID id, Code code, Fp64rec field);

	SystemOp system(ID id, Code code, SystemData field);

	AnyRecOp ptr(ID id, Code code, AnyRec field);

	DataRecOp ptr(ID id, Code code, DataRec field);

	<SS extends StructOp<SS>> StructRecOp<SS> ptr(
			ID id,
			Code code,
			StructRec<SS> field);

	RelRecOp relPtr(ID id, Code code, RelRec field);

	<SS extends StructOp<SS>> SS struct(ID id, Code code, Type<SS> field);

	<F extends Func<F>> FuncOp<F> func(ID id, Code code, FuncRec<F> field);

	<SS extends StructOp<SS>> SS to(ID id, Code code, Type<SS> type);

}
