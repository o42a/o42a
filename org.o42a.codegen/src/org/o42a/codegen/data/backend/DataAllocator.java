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
package org.o42a.codegen.data.backend;

import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public interface DataAllocator {

	DataAllocation<AnyOp> addBinary(
			Ptr<AnyOp> pointer,
			byte[] data,
			int start,
			int end);

	DataAllocation<SystemOp> addSystemType(SystemType systemType);

	<S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			Type<S> type);

	<S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			DataAllocation<S> type,
			Global<S, ?> global);

	<S extends StructOp<S>> DataAllocation<S> enter(
			DataAllocation<?> enclosing,
			SubData<S> data,
			DataAllocation<S> type);

	void exit(DataAllocation<?> enclosing, SubData<?> data);

	void end(Global<?, ?> global);

	void end(Type<?> type);

	DataAllocation<Int8recOp> allocateInt8(
			DataAllocation<?> enclosing,
			Int8rec data,
			DataAllocation<Int8recOp> type);

	DataAllocation<Int16recOp> allocateInt16(
			DataAllocation<?> enclosing,
			Int16rec data,
			DataAllocation<Int16recOp> type);

	DataAllocation<Int32recOp> allocateInt32(
			DataAllocation<?> enclosing,
			Int32rec data,
			DataAllocation<Int32recOp> type);

	DataAllocation<Int64recOp> allocateInt64(
			DataAllocation<?> enclosing,
			Int64rec data,
			DataAllocation<Int64recOp> type);

	DataAllocation<Fp32recOp> allocateFp32(
			DataAllocation<?> enclosing,
			Fp32rec data,
			DataAllocation<Fp32recOp> type);

	DataAllocation<Fp64recOp> allocateFp64(
			DataAllocation<?> enclosing,
			Fp64rec data,
			DataAllocation<Fp64recOp> type);

	DataAllocation<SystemOp> allocateSystem(
			DataAllocation<?> enclosing,
			SystemData data,
			DataAllocation<SystemOp> type);

	<F extends Fn<F>> DataAllocation<FuncOp<F>> allocateFuncPtr(
			DataAllocation<?> enclosing,
			FuncRec<F> data,
			DataAllocation<FuncOp<F>> type,
			Signature<F> signature);

	DataAllocation<AnyRecOp> allocatePtr(
			DataAllocation<?> enclosing,
			AnyRec data,
			DataAllocation<AnyRecOp> type);

	DataAllocation<DataRecOp> allocateDataPtr(
			DataAllocation<?> enclosing,
			DataRec data,
			DataAllocation<DataRecOp> type);

	<S extends StructOp<S>> DataAllocation<StructRecOp<S>> allocatePtr(
			DataAllocation<?> enclosing,
			StructRec<S> data,
			DataAllocation<StructRecOp<S>> type,
			DataAllocation<S> struct);

	DataAllocation<RelRecOp> allocateRelPtr(
			DataAllocation<?> enclosing,
			RelRec data,
			DataAllocation<RelRecOp> type);

	<S extends StructOp<S>> DataAllocation<S> externStruct(
			Ptr<S> pointer,
			DataAllocation<S> type,
			GlobalAttributes attributes);

}
