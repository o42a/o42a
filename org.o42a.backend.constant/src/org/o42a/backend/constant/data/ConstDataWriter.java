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

import java.util.function.Supplier;

import org.o42a.backend.constant.data.func.CFAlloc;
import org.o42a.backend.constant.data.func.NullCFAlloc;
import org.o42a.backend.constant.data.rec.*;
import org.o42a.backend.constant.data.struct.GlobalCDAlloc;
import org.o42a.backend.constant.data.struct.StructCDAlloc;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;


public final class ConstDataWriter implements DataWriter {

	private final ConstBackend backend;

	public ConstDataWriter(ConstBackend backend) {
		this.backend = backend;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	@Override
	public final AnyCDAlloc nullPtr(Ptr<AnyOp> pointer) {
		return new AnyCDAlloc(
				getBackend(),
				pointer,
				new UnderAlloc<AnyOp>() {
					@Override
					public Ptr<AnyOp> allocateUnderlying(
							CDAlloc<AnyOp> alloc) {
						return alloc.getBackend()
								.getUnderlyingGenerator()
								.getGlobals()
								.nullPtr();
					}
				});
	}

	@Override
	public final DataCDAlloc nullDataPtr(Ptr<DataOp> pointer) {
		return new DataCDAlloc(
				getBackend(),
				pointer,
				new UnderAlloc<DataOp>() {
					@Override
					public Ptr<DataOp> allocateUnderlying(
							CDAlloc<DataOp> alloc) {
						return alloc.getBackend()
								.getUnderlyingGenerator()
								.getGlobals()
								.nullDataPtr();
					}
				});
	}

	@Override
	public final <S extends StructOp<S>> StructCDAlloc<S> nullPtr(
			final Ptr<S> pointer,
			final Type<S> type) {
		return new StructCDAlloc<>(
				getBackend(),
				pointer,
				new UnderAlloc<S>() {
					@Override
					public Ptr<S> allocateUnderlying(CDAlloc<S> alloc) {

						final ConstBackend backend = alloc.getBackend();

						return backend.getUnderlyingGenerator()
								.getGlobals()
								.nullPtr(backend.underlying(type));
					}
				});
	}

	@Override
	public final <F extends Fn<F>> CFAlloc<F> nullPtr(FuncPtr<F> pointer) {
		return new NullCFAlloc<>(getBackend(), pointer);
	}

	@Override
	public <S extends StructOp<S>> void begin(
			DataAllocation<S> destination,
			Global<S, ?> global) {
	}

	@Override
	public <S extends StructOp<S>> void enter(
			DataAllocation<S> destination,
			SubData<S> data) {
	}

	@Override
	public <S extends StructOp<S>> void exit(
			DataAllocation<S> destination,
			SubData<S> data) {
	}

	@Override
	public <S extends StructOp<S>> void end(
			DataAllocation<S> destination,
			Global<S, ?> global) {
		if (!global.isExported()) {
			return;
		}

		final GlobalCDAlloc<S> dest = (GlobalCDAlloc<S>) destination;

		dest.getUnderlying();
	}

	@Override
	public void writeInt8(
			DataAllocation<Int8recOp> destination,
			Supplier<Byte> value) {

		final Int8cdAlloc dest = (Int8cdAlloc) destination;

		dest.setValue(value);
	}

	@Override
	public void writeInt16(
			DataAllocation<Int16recOp> destination,
			Supplier<Short> value) {

		final Int16cdAlloc dest = (Int16cdAlloc) destination;

		dest.setValue(value);
	}

	@Override
	public void writeInt32(
			DataAllocation<Int32recOp> destination,
			Supplier<Integer> value) {

		final Int32cdAlloc dest = (Int32cdAlloc) destination;

		dest.setValue(value);
	}

	@Override
	public void writeInt64(
			DataAllocation<Int64recOp> destination,
			Supplier<Long> value) {

		final Int64cdAlloc dest = (Int64cdAlloc) destination;

		dest.setValue(value);
	}

	@Override
	public void writeNativePtrAsInt64(
			DataAllocation<Int64recOp> destination,
			Supplier<Ptr<AnyOp>> value) {

		final Int64cdAlloc dest = (Int64cdAlloc) destination;

		dest.setNativePtr(value);
	}

	@Override
	public void writeFp32(
			DataAllocation<Fp32recOp> destination,
			Supplier<Float> value) {

		final Fp32cdAlloc dest = (Fp32cdAlloc) destination;

		dest.setValue(value);
	}

	@Override
	public void writeFp64(
			DataAllocation<Fp64recOp> destination,
			Supplier<Double> value) {

		final Fp64cdAlloc dest = (Fp64cdAlloc) destination;

		dest.setValue(value);
	}

	@Override
	public void writeSystem(DataAllocation<SystemOp> destination) {
	}

}
