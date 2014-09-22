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
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.string.ID;


public interface CodeWriter {

	boolean created();

	boolean exists();

	ID getId();

	<F extends Fn<F>> FuncCaller<F> caller(
			ID id,
			FuncAllocation<F> allocation);

	CodeWriter inset(Code code);

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

	<F extends Fn<F>> FuncCaller<F> nullPtr(Signature<F> signature);

	AnyRecOp allocatePtr(ID id);

	DataRecOp allocateDataPtr(ID id);

	<S extends StructOp<S>> StructRecOp<S> allocatePtr(
			ID id,
			DataAllocation<S> allocation);

	<S extends StructOp<S>> S allocateStruct(
			ID id,
			DataAllocation<S> allocation);

	<O extends Op> O phi(ID id, O op);

	<O extends Op> O phi(ID id, O op1, O op2);

	<O extends Op> O phi(ID id, O[] ops);

	void acquireBarrier();

	void releaseBarrier();

	void fullBarrier();

}
