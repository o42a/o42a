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
package org.o42a.codegen.code.op;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public interface AnyOp extends DataPtrOp<AnyOp> {

	AnyRecOp toRec(ID id, Code code);

	DataRecOp toDataRec(ID id, Code code);

	Int8recOp toInt8(ID id, Code code);

	Int16recOp toInt16(ID id, Code code);

	Int32recOp toInt32(ID id, Code code);

	Int64recOp toInt64(ID id, Code code);

	Fp32recOp toFp32(ID id, Code code);

	Fp64recOp toFp64(ID id, Code code);

	RelRecOp toRel(ID id, Code code);

	DataOp toData(ID id, Code code);

	CodeOp toCode(ID id, Code code);

	<S extends StructOp<S>> S to(ID id, Code code, Type<S> type);

	<S extends StructOp<S>> StructRecOp<S> toRec(
			ID id,
			Code code,
			Type<S> type);

}
