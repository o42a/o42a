/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.codegen.code;

import org.o42a.codegen.code.Allocated.AllocatedValue;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public abstract class Allocations {

	private static final ID ALLOC_SUFFIX = ID.rawId("__alloc__");

	private final Code code;
	private final Allocated<?> allocated;

	Allocations(Code code, Allocated<?> allocated) {
		this.code = code;
		this.allocated = allocated;
	}

	public final Allocated<?> getAllocated() {
		return this.allocated;
	}

	public final Allocator getAllocator() {
		return getAllocated().getAllocator();
	}

	public final AnyRecOp allocatePtr() {
		return allocatePtr(getAllocated().getId());
	}

	public abstract AnyRecOp allocatePtr(ID id);

	public final DataRecOp allocateDataPtr() {
		return allocateDataPtr(getAllocated().getId());
	}

	public abstract DataRecOp allocateDataPtr(ID id);

	public final <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			Type<S> type) {
		return allocatePtr(getAllocated().getId(), type);
	}

	public abstract <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			ID id,
			Type<S> type);

	public final <S extends StructOp<S>> S allocate(Type<S> type) {
		return allocate(getAllocated().getId(), type);
	}

	public abstract <S extends StructOp<S>> S allocate(ID id, Type<S> type);

	@Override
	public String toString() {
		if (this.code == null) {
			return super.toString();
		}
		return this.code.toString();
	}

	protected final Code code() {
		return this.code;
	}

	static final class ImmediateAllocations<T>
			extends Allocations implements AllocatedValue<T> {

		private boolean isAllocated;
		private T result;

		ImmediateAllocations(Code enclosing, Allocated<T> allocated) {
			super(
					allocated.getAllocatable().getAllocationMode().isMandatory()
					? enclosing
					: enclosing.inset(
							allocated.getId()
							.getLocal()
							.detail(ALLOC_SUFFIX)),
					allocated);
		}

		@Override
		public AnyRecOp allocatePtr(ID id) {
			assert assertIncomplete();
			return writer().allocatePtr(code().opId(id));
		}

		@Override
		public DataRecOp allocateDataPtr(ID id) {
			assert assertIncomplete();
			return writer().allocateDataPtr(code().opId(id));
		}

		@Override
		public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
				ID id,
				Type<S> type) {
			assert assertIncomplete();

			final StructRecOp<S> result = writer().allocatePtr(
					code().opId(id),
					type.data(code().getGenerator()).getPointer().getAllocation());

			result.allocated(code(), null);

			return result;
		}

		@Override
		public <S extends StructOp<S>> S allocate(ID id, Type<S> type) {
			assert assertIncomplete();

			final S result = writer().allocateStruct(
					code().opId(id),
					type.data(code().getGenerator())
					.getPointer()
					.getAllocation());

			result.allocated(code(), null);

			return result;
		}

		@Override
		public final boolean isAllocated() {
			return this.isAllocated;
		}

		@Override
		public T get(Allocator target) {
			if (!isAllocated()) {
				this.isAllocated = true;

				@SuppressWarnings("unchecked")
				final Allocated<T> allocated = (Allocated<T>) getAllocated();

				this.result = allocated.getAllocatable().allocate(
						this,
						allocated);
				allocated.allocated(code(), this.result);
			}

			return this.result;
		}

		private final boolean assertIncomplete() {
			return code().assertIncomplete();
		}

		private final CodeWriter writer() {
			return code().writer();
		}

	}

	static class Reallocations<T> extends Allocations {

		private final AllocationsMap allocationsMap;
		private final Allocator target;
		private int index;

		Reallocations(
				Code code,
				Allocated<T> allocated,
				AllocationsMap allocationsMap,
				Allocator target) {
			super(code, allocated);
			this.allocationsMap = allocationsMap;
			this.target = target;
		}

		@Override
		public AnyRecOp allocatePtr(ID id) {
			return this.allocationsMap.reallocatePtr(
					code(),
					this.target,
					getAllocated(),
					this.index++,
					id);
		}

		@Override
		public DataRecOp allocateDataPtr(ID id) {
			return this.allocationsMap.reallocateDataPtr(
					code(),
					this.target,
					getAllocated(),
					this.index++,
					id);
		}

		@Override
		public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
				ID id,
				Type<S> type) {
			return this.allocationsMap.reallocatePtr(
					code(),
					this.target,
					getAllocated(),
					this.index++,
					id,
					type);
		}

		@Override
		public <S extends StructOp<S>> S allocate(ID id, Type<S> type) {
			return this.allocationsMap.reallocate(
					code(),
					this.target,
					getAllocated(),
					this.index++,
					id,
					type);
		}

		T allocate() {

			@SuppressWarnings("unchecked")
			final Allocated<T> allocated = (Allocated<T>) getAllocated();
			final T result =
					allocated.getAllocatable().allocate(this, allocated);

			allocated.allocated(code(), result);

			return result;
		}

	}

	static class AllocationsRecorder<T> extends Allocations {

		private final AllocationsMap allocationsMap;
		private final Allocator target;
		private int index;

		AllocationsRecorder(
				Code code,
				Allocated<T> allocated,
				AllocationsMap allocationsMap,
				Allocator target) {
			super(code, allocated);
			this.allocationsMap = allocationsMap;
			this.target = target;
		}

		@Override
		public AnyRecOp allocatePtr(ID id) {
			return this.allocationsMap.allocatePtr(
					this.target,
					getAllocated(),
					this.index++,
					id);
		}

		@Override
		public DataRecOp allocateDataPtr(ID id) {
			return this.allocationsMap.allocateDataPtr(
					this.target,
					getAllocated(),
					this.index++,
					id);
		}

		@Override
		public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
				ID id,
				Type<S> type) {
			return this.allocationsMap.allocatePtr(
					this.target,
					getAllocated(),
					this.index++,
					id,
					type);
		}

		@Override
		public <S extends StructOp<S>> S allocate(ID id, Type<S> type) {
			return this.allocationsMap.allocate(
					this.target,
					getAllocated(),
					this.index++,
					id,
					type);
		}

		T allocate() {

			@SuppressWarnings("unchecked")
			final Allocated<T> allocated = (Allocated<T>) getAllocated();

			return allocated.getAllocatable().allocate(this, allocated);
		}

	}

}
