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
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public interface FuncCaller<F extends Func<F>> extends PtrOp {

	Signature<F> getSignature();

	void call(Code code, Op... args);

	Int8op callInt8(CodeId id, Code code, Op... args);

	Int16op callInt16(CodeId id, Code code, Op... args);

	Int32op callInt32(CodeId id, Code code, Op... args);

	Int64op callInt64(CodeId id, Code code, Op... args);

	Fp32op callFp32(CodeId id, Code code, Op... args);

	Fp64op callFp64(CodeId id, Code code, Op... args);

	BoolOp callBool(CodeId id, Code code, Op... args);

	AnyOp callAny(CodeId id, Code code, Op... args);

	DataOp callData(CodeId id, Code code, Op... args);

	<O extends StructOp> O callPtr(
			CodeId id,
			Code code,
			Type<O> type,
			Op... args);

}
