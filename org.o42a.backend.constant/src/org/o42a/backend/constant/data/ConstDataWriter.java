/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.backend.constant.data;

import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncAllocation;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;


public class ConstDataWriter implements DataWriter {

	private final ConstBackend backend;

	public ConstDataWriter(ConstBackend backend) {
		this.backend = backend;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	@Override
	public DataAllocation<AnyOp> nullPtr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<DataOp> nullDataPtr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> nullPtr(Type<S> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F extends Func<F>> FuncAllocation<F> nullPtr(Signature<F> signature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> void begin(
			DataAllocation<S> allocation,
			Global<S, ?> global) {
		// TODO Auto-generated method stub

	}

	@Override
	public <S extends StructOp<S>> void enter(
			DataAllocation<S> allocation,
			SubData<S> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public <S extends StructOp<S>> void exit(
			DataAllocation<S> allocation,
			SubData<S> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public <S extends StructOp<S>> void end(
			DataAllocation<S> allocation,
			Global<S, ?> global) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeInt8(DataAllocation<Int8recOp> allocation, byte value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeInt16(DataAllocation<Int16recOp> allocation, short value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeInt32(DataAllocation<Int32recOp> allocation, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeInt64(DataAllocation<Int64recOp> allocation, long value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeNativePtrAsInt64(
			DataAllocation<Int64recOp> allocation,
			DataAllocation<AnyOp> valueAllocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeFp32(DataAllocation<Fp32recOp> allocation, float value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeFp64(DataAllocation<Fp64recOp> allocation, double value) {
		// TODO Auto-generated method stub

	}

}
