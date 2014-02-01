/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Ptr;


public abstract class UnderAlloc<P extends DataPtrOp<P>> {

	@SuppressWarnings("rawtypes")
	private static final Default DEFAULT_UNDER_ALLOC = new Default();

	@SuppressWarnings("unchecked")
	public static <P extends AllocPtrOp<P>> UnderAlloc<P> defaultUnderAlloc() {
		return DEFAULT_UNDER_ALLOC;
	}

	public static UnderAlloc<AnyOp> anyUnderAlloc(CDAlloc<?> source) {
		return new ToAny(source);
	}

	public static UnderAlloc<DataOp> dataUnderAlloc(CDAlloc<?> source) {
		return new ToData(source);
	}

	public final boolean isDefault() {
		return this == DEFAULT_UNDER_ALLOC;
	}

	public abstract Ptr<P> allocateUnderlying(CDAlloc<P> alloc);

	private static final class Default<P extends AllocPtrOp<P>>
			extends UnderAlloc<P> {

		private Default() {
		}

		@Override
		public Ptr<P> allocateUnderlying(CDAlloc<P> alloc) {

			@SuppressWarnings("unchecked")
			final DCDAlloc<P, ?> dcdAlloc = (DCDAlloc<P, ?>) alloc;

			return dcdAlloc.getUnderlying().getPointer();
		}

		@Override
		public String toString() {
			return "DefaultUnderAlloc";
		}

	}

	private static final class ToAny extends UnderAlloc<AnyOp> {

		private final CDAlloc<?> source;

		ToAny(CDAlloc<?> source) {
			this.source = source;
		}

		@Override
		public Ptr<AnyOp> allocateUnderlying(CDAlloc<AnyOp> alloc) {
			return this.source.getUnderlyingPtr().toAny();
		}

		@Override
		public String toString() {
			return "(any*) " + this.source;
		}

	}

	private static final class ToData extends UnderAlloc<DataOp> {

		private final CDAlloc<?> source;

		ToData(CDAlloc<?> source) {
			this.source = source;
		}

		@Override
		public Ptr<DataOp> allocateUnderlying(CDAlloc<DataOp> alloc) {
			return this.source.getUnderlyingPtr().toData();
		}

		@Override
		public String toString() {
			return "(data*) " + this.source;
		}

	}

}
