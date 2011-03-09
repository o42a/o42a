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


public interface PtrOp extends Op {

	void returnValue(Code code);

	BoolOp isNull(Code code);

	BoolOp eq(Code code, PtrOp other);

	AnyOp toAny(Code code);

	DataOp<AnyOp> toPtr(Code code);

	DataOp<Int32op> toInt32(Code code);

	DataOp<Int64op> toInt64(Code code);

	DataOp<Fp64op> toFp64(Code code);

	DataOp<RelOp> toRel(Code code);

	<F extends Func> CodeOp<F> toFunc(Code code, Signature<F> signature);

	<O extends StructOp> O to(Code code, Type<O> type);

}
