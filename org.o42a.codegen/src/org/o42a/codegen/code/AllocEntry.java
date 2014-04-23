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

import static org.o42a.util.ArrayUtil.singleElementArray;

import java.util.Arrays;
import java.util.IdentityHashMap;

import org.o42a.codegen.code.AllocRecords.AllocRecord;
import org.o42a.codegen.code.Allocations.AllocationsRecorder;
import org.o42a.codegen.code.op.AllocPtrOp;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID;


final class AllocEntry {

	private static final Code[] NO_ALLOCS = new Code[0];
	private static final ID REALLOC_ID = ID.rawId("__realloc__");
	private static final ID PREALLOC_SUFFIX = ID.rawId("__prealloc__");

	private final AllocationsMap allocationsMap;
	private final Allocator target;
	private Code[] reallocs = NO_ALLOCS;
	private IdentityHashMap<Allocated<?>, AllocatedData<?>> data;
	private boolean combined;

	AllocEntry(AllocationsMap allocationsMap, Allocator target) {
		this.allocationsMap = allocationsMap;
		this.target = target;
	}

	public final Allocator getAllocator() {
		return this.allocationsMap.getAllocator();
	}

	public final AllocationsMap getAllocationsMap() {
		return this.allocationsMap;
	}

	public final Allocator getTarget() {
		return this.target;
	}

	public final boolean isEmpty() {
		return this.data == null && this.reallocs.length == 0;
	}

	public final boolean isCombined() {
		return this.combined;
	}

	public void allocateIn(Code code) {

		final Code realloc = code.inset(REALLOC_ID);

		this.reallocs = ArrayUtil.append(this.reallocs, realloc);
		if (this.data != null) {
			for (AllocatedData<?> data : this.data.values()) {
				data.allocateIn(realloc);
			}
		}
	}

	public <T> AllocatedData<T> data(Allocated<T> allocated) {
		if (this.data == null) {
			this.data = new IdentityHashMap<>();
		} else {

			@SuppressWarnings("unchecked")
			final AllocatedData<T> existing =
					(AllocatedData<T>) this.data.get(allocated);

			if (existing != null) {
				return existing;
			}
		}

		final AllocatedData<T> data = new AllocatedData<>(this, allocated);

		this.data.put(allocated, data);
		data.initParents();
		for (Code realloc : this.reallocs) {
			data.allocateIn(realloc);
		}

		return data;
	}

	public void combineAll() {
		if (isCombined()) {
			return;
		}
		this.combined = true;

		final AllocEntry parentEntry = parentEntry();

		if (parentEntry != null) {
			parentEntry.combineAll();
		}
		if (this.data != null) {
			for (AllocatedData<?> data :
					this.data.values().toArray(
							new AllocatedData[this.data.size()])) {
				data.combine();
			}
		}
	}

	public void checkAllCombined() {
		assert isCombined() :
			"Not combined";
		if (this.data != null) {
			for (AllocatedData<?> data : this.data.values()) {
				data.checkCombined();
			}
		}
	}

	private final AllocEntry parentEntry() {
		if (this.target == getAllocator()) {
			return null;
		}
		return this.allocationsMap.findEntry(
				this.target.getEnclosingAllocator());
	}

	static final class AllocatedData<T> {

		private final AllocEntry entry;
		private final Allocated<T> allocated;
		private AllocRecords records;
		private AllocatedPtrs<?>[] ptrs;
		private T result;
		private boolean isAllocated;

		private AllocatedData(AllocEntry entry, Allocated<T> allocated) {
			this.entry = entry;
			this.allocated = allocated;
		}

		public boolean isEmpty() {
			return this.ptrs == null && this.entry.reallocs.length == 0;
		}

		public T get() {
			if (!this.isAllocated) {
				this.isAllocated = true;

				final AllocationsRecorder<T> allocations =
						new Allocations.AllocationsRecorder<>(
								this.entry.target.allocations(),
								this.allocated,
								this.entry.allocationsMap,
								this.entry.target);

				this.result = allocations.allocate();
			}

			return this.result;
		}

		@SuppressWarnings("unchecked")
		public <P extends AllocPtrOp<P>> AllocatedPtrs<P> ptrs(
				AllocRecord<P> record) {

			final int index = record.getIndex();

			return (AllocatedPtrs<P>) ptrsContaining(index)[index];
		}

		private final AllocationsMap targetMap() {
			return this.entry.target.allocationsMap();
		}

		private AllocatedData<T> parentData() {

			final AllocEntry parentEntry = this.entry.parentEntry();

			if (parentEntry == null) {
				return null;
			}

			return parentEntry.data(this.allocated);
		}

		private void initParents() {

			final AllocatedData<T> parentData = parentData();

			if (parentData != null) {
				parentData.initParents();
			}
		}

		private void allocateIn(Code code) {

			final Allocations.Reallocations<T> allocations =
					new Allocations.Reallocations<>(
							code,
							this.allocated,
							this.entry.allocationsMap,
							this.entry.target);

			allocations.allocate();
		}

		private void combine() {
			for (AllocatedPtrs<?> ptrs : allPtrs()) {
				ptrs.combine();
			}
		}

		private void checkCombined() {
			for (AllocatedPtrs<?> ptrs : allPtrs()) {
				ptrs.checkCombined();
			}
		}

		private AllocatedPtrs<?>[] allPtrs() {
			return ptrsContaining(records().size() - 1);
		}

		private final AllocatedPtrs<?>[] ptrsContaining(int index) {

			final int fillFrom;
			final AllocRecords records;

			if (this.ptrs == null) {
				fillFrom = 0;
				records = records();
				this.ptrs = new AllocatedPtrs[records.size()];
			} else if (this.ptrs.length > index) {
				return this.ptrs;
			} else {
				fillFrom = this.ptrs.length;
				records = records();
				this.ptrs = Arrays.copyOf(this.ptrs, records.size());
			}

			for (int i = fillFrom; i < this.ptrs.length; ++i) {

				final AllocRecord<?> record = records.get(i);

				this.ptrs[i] = newPtrs(record);
			}

			return this.ptrs;
		}

		private AllocRecords records() {
			if (this.records != null) {
				return this.records;
			}
			return this.records =
					this.entry.getAllocationsMap().records(this.allocated);
		}

		private <P extends AllocPtrOp<P>> AllocatedPtrs<P> newPtrs(
				AllocRecord<P> record) {
			return new AllocatedPtrs<>(this, record);
		}

	}

	static final class AllocatedPtrs<P extends AllocPtrOp<P>> {

		private final AllocatedData<?> data;
		private final AllocRecord<P> record;
		private AnyRecOp prealloc;
		private P ptr;
		private P[] ptrs;
		private boolean combined;

		private AllocatedPtrs(AllocatedData<?> data, AllocRecord<P> record) {
			this.data = data;
			this.record = record;
		}

		public final boolean isEmpty() {
			return this.ptr == null && this.ptrs == null;
		}

		public P ptr() {
			if (this.ptr != null) {
				return this.ptr;
			}

			final Code code = targetMap().merges();

			this.ptr = this.record.load(code, prealloc());

			if (this.data.entry.isCombined()) {
				// May happen during the dispose.
				// Request combining immediately.
				combine();
			}

			return this.ptr;
		}

		public P add(Code code) {
			assert !this.combined :
				"Pointers already combined. Can not add a new one";

			final P ptr = this.record.allocate(code);

			if (this.ptrs == null) {
				this.ptrs = singleElementArray(ptr);
			} else {
				this.ptrs = ArrayUtil.append(this.ptrs, ptr);
			}

			return ptr;
		}

		private AllocatedPtrs<P> parentPtrs() {

			final AllocatedData<?> parentData = this.data.parentData();

			if (parentData == null) {
				return null;
			}

			return parentData.ptrs(this.record);
		}

		private AnyRecOp prealloc() {
			if (this.prealloc != null) {
				return this.prealloc;
			}

			final Code code = targetMap().entry();

			return this.prealloc = code.writer().allocatePtr(
					this.record.getId().detail(PREALLOC_SUFFIX));
		}

		private void combine() {
			if (this.combined || isEmpty()) {
				return;
			}
			this.combined = true;

			final AllocatedPtrs<P> parentPtrs = parentPtrs();
			final P[] ptrs;

			if (parentPtrs == null) {
				assert this.ptrs != null :
					"No allocations to combine";
				ptrs = this.ptrs;
			} else {
				parentPtrs.combine();
				if (this.ptrs == null) {
					ptrs = singleElementArray(parentPtrs.ptr());
				} else {
					ptrs = Arrays.copyOf(this.ptrs, this.ptrs.length + 1);
					ptrs[this.ptrs.length] = parentPtrs.ptr();
				}
			}

			this.record.combine(targetMap(), prealloc(), ptrs);
		}

		private void checkCombined() {
			assert this.combined :
				"Not combined";
		}

		private final AllocationsMap targetMap() {
			return this.data.targetMap();
		}

	}

}
