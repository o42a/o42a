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
package org.o42a.codegen.code.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.data.Type;


public interface AnyOp extends PtrOp {

	AnyRecOp toPtr(CodeId id, Code code);

	Int8recOp toInt8(CodeId id, Code code);

	Int16recOp toInt16(CodeId id, Code code);

	Int32recOp toInt32(CodeId id, Code code);

	Int64recOp toInt64(CodeId id, Code code);

	Fp32recOp toFp32(CodeId id, Code code);

	Fp64recOp toFp64(CodeId id, Code code);

	RelRecOp toRel(CodeId id, Code code);

	DataOp toData(CodeId id, Code code);

	<F extends Func> FuncOp<F> toFunc(
			CodeId id,
			Code code,
			Signature<F> signature);

	<S extends StructOp> S to(CodeId id, Code code, Type<S> type);

}
