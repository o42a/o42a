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

	DataAllocation<AnyOp> addBinary(CodeId id, byte[] data, int start, int end);

	<O extends StructOp> DataAllocation<O> begin(Type<O> type);

	<O extends StructOp> DataAllocation<O> begin(
			DataAllocation<O> type,
			Global<O, ?> global);

	<O extends StructOp> DataAllocation<O> enter(
			DataAllocation<O> type,
			SubData<O> data);

	void exit(SubData<?> data);

	void end(Global<?, ?> global);

	void end(Type<?> type);

	DataAllocation<DataOp<Int32op>> allocateInt32(
			DataAllocation<DataOp<Int32op>> type);

	DataAllocation<DataOp<Int64op>> allocateInt64(
			DataAllocation<DataOp<Int64op>> type);

	DataAllocation<DataOp<Fp64op>> allocateFp64(
			DataAllocation<DataOp<Fp64op>> allocation);

	<F extends Func> DataAllocation<CodeOp<F>> allocateCodePtr(
			DataAllocation<CodeOp<F>> type,
			Signature<F> signature);

	DataAllocation<AnyOp> allocatePtr(DataAllocation<AnyOp> type);

	<P extends StructOp> DataAllocation<P> allocatePtr(
			DataAllocation<P> type,
			DataAllocation<P> struct);

	DataAllocation<DataOp<RelOp>> allocateRelPtr(
			DataAllocation<DataOp<RelOp>> type);

}
