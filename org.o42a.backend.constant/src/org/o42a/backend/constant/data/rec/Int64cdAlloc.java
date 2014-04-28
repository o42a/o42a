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
package org.o42a.backend.constant.data.rec;

import static org.o42a.backend.constant.code.rec.RecStore.allocRecStore;

import java.util.function.Supplier;

import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.rec.Int64recCOp;
import org.o42a.backend.constant.data.AnyCDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.codegen.data.*;


public final class Int64cdAlloc extends RecCDAlloc<Int64rec, Int64recOp, Long> {

	private Supplier<Ptr<AnyOp>> nativePtr;

	public Int64cdAlloc(
			ContainerCDAlloc<?> enclosing,
			Int64rec data,
			Int64cdAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	public final void setValue(Supplier<Long> value) {
		super.setValue(value);
		this.nativePtr = null;
	}

	public final void setNativePtr(Supplier<Ptr<AnyOp>> pointer) {
		this.nativePtr = pointer;
		super.setValue(null);
		if (isUnderlyingAllocated()) {
			getUnderlying().setNativePtr(new UnderlyingNativePtr(pointer));
		}
	}

	@Override
	public Long underlyingValue(Long value) {
		return value;
	}

	@Override
	protected Int64rec allocateUnderlying(SubData<?> container) {

		final Int64rec underlying = super.allocateUnderlying(container);

		if (this.nativePtr != null) {
			underlying.setNativePtr(new UnderlyingNativePtr(this.nativePtr));
		}

		return underlying;
	}

	@Override
	protected Int64rec allocateUnderlying(SubData<?> container, String name) {
		return container.addInt64(name);
	}

	@Override
	protected Int64recOp op(OpBE<Int64recOp> backend, AllocPlace allocPlace) {
		return new Int64recCOp(backend, allocRecStore(allocPlace), getPointer());
	}

	private static final class UnderlyingNativePtr implements Supplier<Ptr<AnyOp>> {

		private final Supplier<Ptr<AnyOp>> nativePtr;

		UnderlyingNativePtr(Supplier<Ptr<AnyOp>> nativePtr) {
			this.nativePtr = nativePtr;
		}

		@Override
		public Ptr<AnyOp> get() {

			final AnyCDAlloc alloc =
					(AnyCDAlloc) this.nativePtr.get().getAllocation();

			return alloc.getUnderlyingPtr();
		}

		@Override
		public String toString() {
			return String.valueOf(this.nativePtr);
		}

	}

}
