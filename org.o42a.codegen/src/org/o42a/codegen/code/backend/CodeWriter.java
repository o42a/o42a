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
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.backend.DataAllocation;


public interface CodeWriter {

	CodePos head();

	CodePos tail();

	void done();

	CodeId getId();

	<F extends Func> FuncCaller<F> caller(
			CodeId id,
			FuncAllocation<F> allocation);

	CodeWriter block(Code code, CodeId id);

	AllocationWriter allocationBlock(AllocationCode code, CodeId id);

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

	<O extends StructOp> O nullPtr(DataAllocation<O> type);

	<F extends Func> FuncCaller<F> nullPtr(Signature<F> signature);

	void go(CodePos pos);

	void go(BoolOp condition, CodePos truePos, CodePos falsePos);

	MultiCodePos comeFrom(CodeWriter[] alts);

	void goToOneOf(MultiCodePos target);

	AnyRecOp allocatePtr(CodeId id);

	<S extends StructOp> StructRecOp<S> allocatePtr(
			CodeId id,
			DataAllocation<S> allocation);

	<O extends StructOp> O allocateStruct(
			CodeId id,
			DataAllocation<O> allocation);

	<O extends Op> O phi(CodeId id, O op);

	<O extends Op> O phi(CodeId id, O op1, O op2);

	void returnVoid();

}
