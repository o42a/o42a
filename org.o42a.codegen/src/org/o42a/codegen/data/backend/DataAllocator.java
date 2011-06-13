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
package org.o42a.codegen.data.backend;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public interface DataAllocator {

	DataAllocation<AnyOp> addBinary(
			CodeId id,
			boolean isConstant,
			byte[] data,
			int start,
			int end);

	<O extends StructOp> DataAllocation<O> begin(Type<O> type);

	<O extends StructOp> DataAllocation<O> begin(
			DataAllocation<O> type,
			Global<O, ?> global);

	<O extends StructOp> DataAllocation<O> enter(
			DataAllocation<?> enclosing,
			DataAllocation<O> type,
			SubData<O> data);

	void exit(DataAllocation<?> enclosing, SubData<?> data);

	void end(Global<?, ?> global);

	void end(Type<?> type);

	DataAllocation<RecOp<Int8op>> allocateInt8(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Int8op>> type);

	DataAllocation<RecOp<Int16op>> allocateInt16(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Int16op>> type);

	DataAllocation<RecOp<Int32op>> allocateInt32(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Int32op>> type);

	DataAllocation<RecOp<Int64op>> allocateInt64(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Int64op>> type);

	DataAllocation<RecOp<Fp32op>> allocateFp32(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Fp32op>> allocation);

	DataAllocation<RecOp<Fp64op>> allocateFp64(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<Fp64op>> allocation);

	<F extends Func> DataAllocation<FuncOp<F>> allocateFuncPtr(
			DataAllocation<?> enclosing,
			DataAllocation<FuncOp<F>> type,
			Signature<F> signature);

	DataAllocation<AnyOp> allocatePtr(
			DataAllocation<?> enclosing,
			DataAllocation<AnyOp> type);

	DataAllocation<DataOp> allocateDataPtr(
			DataAllocation<?> enclosing,
			DataAllocation<DataOp> type);

	<P extends StructOp> DataAllocation<P> allocatePtr(
			DataAllocation<?> enclosing,
			DataAllocation<P> type,
			DataAllocation<P> struct);

	DataAllocation<RecOp<RelOp>> allocateRelPtr(
			DataAllocation<?> enclosing,
			DataAllocation<RecOp<RelOp>> type);

}
