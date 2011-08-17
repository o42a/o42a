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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;


public class ConstDataAllocator implements DataAllocator {

	private final ConstBackend backend;

	public ConstDataAllocator(ConstBackend backend) {
		this.backend = backend;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	@Override
	public DataAllocation<AnyOp> addBinary(
			CodeId id,
			boolean isConstant,
			byte[] data,
			int start,
			int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(Type<S> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			DataAllocation<S> type,
			Global<S, ?> global) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> enter(
			DataAllocation<?> enclosing,
			DataAllocation<S> type,
			SubData<S> data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void exit(DataAllocation<?> enclosing, SubData<?> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void end(Global<?, ?> global) {
		// TODO Auto-generated method stub

	}

	@Override
	public void end(Type<?> type) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataAllocation<Int8recOp> allocateInt8(
			DataAllocation<?> enclosing,
			DataAllocation<Int8recOp> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<Int16recOp> allocateInt16(
			DataAllocation<?> enclosing,
			DataAllocation<Int16recOp> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<Int32recOp> allocateInt32(
			DataAllocation<?> enclosing,
			DataAllocation<Int32recOp> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<Int64recOp> allocateInt64(
			DataAllocation<?> enclosing,
			DataAllocation<Int64recOp> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<Fp32recOp> allocateFp32(
			DataAllocation<?> enclosing,
			DataAllocation<Fp32recOp> allocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<Fp64recOp> allocateFp64(
			DataAllocation<?> enclosing,
			DataAllocation<Fp64recOp> allocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F extends Func<F>> DataAllocation<FuncOp<F>> allocateFuncPtr(
			DataAllocation<?> enclosing,
			DataAllocation<FuncOp<F>> type,
			Signature<F> signature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<AnyOp> allocatePtr(
			DataAllocation<?> enclosing,
			DataAllocation<AnyOp> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<DataOp> allocateDataPtr(
			DataAllocation<?> enclosing,
			DataAllocation<DataOp> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> allocatePtr(
			DataAllocation<?> enclosing,
			DataAllocation<S> type,
			DataAllocation<S> struct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAllocation<RelRecOp> allocateRelPtr(
			DataAllocation<?> enclosing,
			DataAllocation<RelRecOp> type) {
		// TODO Auto-generated method stub
		return null;
	}

}
