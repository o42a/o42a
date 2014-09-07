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

import org.o42a.codegen.code.Arg;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public interface SignatureWriter<F extends Fn<F>> {

	void returnVoid();

	void returnInt8();

	void returnInt16();

	void returnInt32();

	void returnInt64();

	void returnFp32();

	void returnFp64();

	void returnBool();

	void returnAny();

	void returnData();

	void returnPtr(Type<?> type);

	void addInt8(Arg<Int8op> arg);

	void addInt16(Arg<Int16op> arg);

	void addInt32(Arg<Int32op> arg);

	void addInt64(Arg<Int64op> arg);

	void addFp32(Arg<Fp32op> arg);

	void addFp64(Arg<Fp64op> arg);

	void addBool(Arg<BoolOp> arg);

	void addRelPtr(Arg<RelOp> arg);

	void addPtr(Arg<AnyOp> arg);

	void addData(Arg<DataOp> arg);

	<S extends StructOp<S>> void addPtr(Arg<S> arg, Type<S> type);

	<FF extends Fn<FF>> void addFuncPtr(Arg<FF> arg, Signature<FF> signature);

	SignatureAllocation<F> done();

}
