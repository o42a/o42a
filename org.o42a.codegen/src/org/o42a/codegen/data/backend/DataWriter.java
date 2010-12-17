/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.CodeAllocation;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public interface DataWriter {

	DataAllocation<AnyOp> nullPtr();

	<O extends PtrOp> DataAllocation<O> nullPtr(Type<O> type);

	<F extends Func> CodeAllocation<F> nullPtr(Signature<F> signature);

	<O extends PtrOp> void begin(
			DataAllocation<O> allocation,
			Global<O, ?> global);

	<O extends PtrOp> void enter(DataAllocation<O> allocation, SubData<O> data);

	<O extends PtrOp> void exit(DataAllocation<O> allocation, SubData<O> data);

	<O extends PtrOp> void end(
			DataAllocation<O> allocation,
			Global<O, ?> global);

	void writeInt32(DataAllocation<DataOp<Int32op>> allocation, int value);

	void writeInt64(DataAllocation<DataOp<Int64op>> allocation, long value);

	void writeNativePtrAsInt64(
			DataAllocation<DataOp<Int64op>> allocation,
			DataAllocation<AnyOp> valueAllocation);

	void writeFp64(DataAllocation<DataOp<Fp64op>> allocation, Double value);

}
