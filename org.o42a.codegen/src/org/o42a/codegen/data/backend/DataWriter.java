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
package org.o42a.codegen.data.backend;

import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public interface DataWriter {

	DataAllocation<AnyOp> nullPtr();

	DataAllocation<DataOp> nullDataPtr();

	<S extends StructOp<S>> DataAllocation<S> nullPtr(Type<S> type);

	<F extends Func<F>> FuncAllocation<F> nullPtr(FuncPtr<F> pointer);

	<S extends StructOp<S>> void begin(
			DataAllocation<S> destination,
			Global<S, ?> global);

	<S extends StructOp<S>> void enter(
			DataAllocation<S> destination,
			SubData<S> data);

	<S extends StructOp<S>> void exit(
			DataAllocation<S> destination,
			SubData<S> data);

	<S extends StructOp<S>> void end(
			DataAllocation<S> destination,
			Global<S, ?> global);

	void writeInt8(DataAllocation<Int8recOp> destination, byte value);

	void writeInt16(DataAllocation<Int16recOp> destination, short value);

	void writeInt32(DataAllocation<Int32recOp> destination, int value);

	void writeInt64(DataAllocation<Int64recOp> destination, long value);

	void writeNativePtrAsInt64(
			DataAllocation<Int64recOp> destination,
			DataAllocation<AnyOp> valueAllocation);

	void writeFp32(DataAllocation<Fp32recOp> destination, float value);

	void writeFp64(DataAllocation<Fp64recOp> destination, double value);

}
