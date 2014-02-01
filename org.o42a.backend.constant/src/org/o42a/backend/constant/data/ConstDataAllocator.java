/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.backend.constant.data.rec.*;
import org.o42a.backend.constant.data.struct.*;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
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
			final Ptr<AnyOp> pointer,
			final byte[] data,
			final int start,
			final int end) {
		return new AnyCDAlloc(
				getBackend(),
				pointer,
				new UnderAlloc<AnyOp>() {
					@Override
					public Ptr<AnyOp> allocateUnderlying(
							CDAlloc<AnyOp> alloc) {
						return alloc.getBackend()
								.getUnderlyingGenerator()
								.addBinary(
										pointer.getId(),
										pointer.isPtrToConstant(),
										data,
										start,
										end);
					}
				});
	}

	@Override
	public DataAllocation<SystemOp> addSystemType(SystemType systemType) {
		return new SystemTypeCDAlloc(getBackend(), systemType);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			Type<S> type) {
		return new TypeCDAlloc<>(getBackend(), data, type);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> begin(
			SubData<S> data,
			DataAllocation<S> type,
			Global<S, ?> global) {
		return new GlobalCDAlloc<>(
				getBackend(),
				data,
				(ContainerCDAlloc<S>) type,
				global);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> enter(
			DataAllocation<?> enclosing,
			SubData<S> data,
			DataAllocation<S> type) {
		return new StructCDAlloc<>(
				enclosing(enclosing),
				data,
				(ContainerCDAlloc<S>) type);
	}

	@Override
	public void exit(DataAllocation<?> enclosing, SubData<?> data) {

		final ContainerCDAlloc<?> alloc =
				(StructCDAlloc<?>) data.getPointer().getAllocation();

		alloc.containerAllocated();
	}

	@Override
	public void end(Global<?, ?> global) {

		final ContainerCDAlloc<?> alloc =
				(GlobalCDAlloc<?>) global.getPointer().getAllocation();

		alloc.containerAllocated();
	}

	@Override
	public void end(Type<?> type) {

		final ContainerCDAlloc<?> alloc = (TypeCDAlloc<?>) type.pointer(
				getBackend().getGenerator()).getAllocation();

		alloc.containerAllocated();
	}

	@Override
	public final Int8cdAlloc allocateInt8(
			DataAllocation<?> enclosing,
			Int8rec data,
			DataAllocation<Int8recOp> type) {
		return new Int8cdAlloc(enclosing(enclosing), data, (Int8cdAlloc) type);
	}

	@Override
	public final Int16cdAlloc allocateInt16(
			DataAllocation<?> enclosing,
			Int16rec data,
			DataAllocation<Int16recOp> type) {
		return new Int16cdAlloc(
				enclosing(enclosing),
				data,
				(Int16cdAlloc) type);
	}

	@Override
	public final Int32cdAlloc allocateInt32(
			DataAllocation<?> enclosing,
			Int32rec data,
			DataAllocation<Int32recOp> type) {
		return new Int32cdAlloc(
				enclosing(enclosing),
				data,
				(Int32cdAlloc) type);
	}

	@Override
	public final Int64cdAlloc allocateInt64(
			DataAllocation<?> enclosing,
			Int64rec data,
			DataAllocation<Int64recOp> type) {
		return new Int64cdAlloc(
				enclosing(enclosing),
				data,
				(Int64cdAlloc) type);
	}

	@Override
	public final Fp32cdAlloc allocateFp32(
			DataAllocation<?> enclosing,
			Fp32rec data,
			DataAllocation<Fp32recOp> type) {
		return new Fp32cdAlloc(
				enclosing(enclosing),
				data,
				(Fp32cdAlloc) type);
	}

	@Override
	public final Fp64cdAlloc allocateFp64(
			DataAllocation<?> enclosing,
			Fp64rec data,
			DataAllocation<Fp64recOp> type) {
		return new Fp64cdAlloc(
				enclosing(enclosing),
				data,
				(Fp64cdAlloc) type);
	}

	@Override
	public SystemCDAlloc allocateSystem(
			DataAllocation<?> enclosing,
			SystemData data,
			DataAllocation<SystemOp> type) {
		return new SystemCDAlloc(
				enclosing(enclosing),
				data,
				(SystemCDAlloc) type);
	}

	@Override
	public <F extends Func<F>> FuncRecCDAlloc<F> allocateFuncPtr(
			DataAllocation<?> enclosing,
			FuncRec<F> data,
			DataAllocation<FuncOp<F>> type,
			Signature<F> signature) {
		return new FuncRecCDAlloc<>(
				enclosing(enclosing),
				data,
				(FuncRecCDAlloc<F>) type,
				signature);
	}

	@Override
	public final AnyRecCDAlloc allocatePtr(
			DataAllocation<?> enclosing,
			AnyRec data,
			DataAllocation<AnyRecOp> type) {
		return new AnyRecCDAlloc(
				enclosing(enclosing),
				data,
				(AnyRecCDAlloc) type);
	}

	@Override
	public final DataRecCDAlloc allocateDataPtr(
			DataAllocation<?> enclosing,
			DataRec data,
			DataAllocation<DataRecOp> type) {
		return new DataRecCDAlloc(
				enclosing(enclosing),
				data,
				(DataRecCDAlloc) type);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<StructRecOp<S>> allocatePtr(
			DataAllocation<?> enclosing,
			StructRec<S> data,
			DataAllocation<StructRecOp<S>> type,
			DataAllocation<S> struct) {
		return new StructRecCDAlloc<>(
				enclosing(enclosing),
				data,
				(StructRecCDAlloc<S>) type,
				(ContainerCDAlloc<S>) struct);
	}

	@Override
	public final RelRecCDAlloc allocateRelPtr(
			DataAllocation<?> enclosing,
			RelRec data,
			DataAllocation<RelRecOp> type) {
		return new RelRecCDAlloc(
				enclosing(enclosing),
				data,
				(RelRecCDAlloc) type);
	}

	@Override
	public <S extends StructOp<S>> DataAllocation<S> externStruct(
			Ptr<S> pointer,
			DataAllocation<S> type,
			GlobalAttributes attributes) {
		return new ExternCDAlloc<>(
				getBackend(),
				pointer,
				((ContainerCDAlloc<S>) type).getType(),
				attributes);
	}

	private static ContainerCDAlloc<?> enclosing(DataAllocation<?> enclosing) {
		return (ContainerCDAlloc<?>) enclosing;
	}

}
