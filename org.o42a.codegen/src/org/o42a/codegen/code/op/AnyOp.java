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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.data.Type;


public interface AnyOp extends PtrOp {

	RecOp<AnyOp> toPtr(String name, Code code);

	RecOp<Int8op> toInt8(String name, Code code);

	RecOp<Int16op> toInt16(String name, Code code);

	RecOp<Int32op> toInt32(String name, Code code);

	RecOp<Int64op> toInt64(String name, Code code);

	RecOp<Fp32op> toFp32(String name, Code code);

	RecOp<Fp64op> toFp64(String name, Code code);

	RecOp<RelOp> toRel(String name, Code code);

	DataOp toData(String name, Code code);

	<F extends Func> FuncOp<F> toFunc(
			String name,
			Code code,
			Signature<F> signature);

	<O extends StructOp> O to(String name, Code code, Type<O> type);

}
