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
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;


public interface CodeWriter {

	boolean created();

	boolean exists();

	void done();

	CodeId getId();

	<F extends Func<F>> FuncCaller<F> caller(
			CodeId id,
			FuncAllocation<F> allocation);

	CodeWriter inset(Code code);

	AllocationWriter allocation(AllocationCode code);

	BlockWriter block(Block code);

	Int8op int8(byte value);

	Int16op int16(short value);

	Int32op int32(int value);

	Int64op int64(long value);

	Fp32op fp32(float value);

	Fp64op fp64(double value);

	BoolOp bool(boolean value);

	RelOp nullRelPtr();

	AnyOp nullPtr();

	DataOp nullDataPtr();

	<S extends StructOp<S>> S nullPtr(DataAllocation<S> type);

	<F extends Func<F>> FuncCaller<F> nullPtr(Signature<F> signature);

	<O extends Op> O phi(CodeId id, O op);

	<O extends Op> O phi(CodeId id, O op1, O op2);

}
