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

import static org.o42a.codegen.code.AllocAsset.allocatedAsset;

import java.util.IdentityHashMap;

import org.o42a.codegen.code.AllocRecords.AllocRecord;
import org.o42a.codegen.code.Allocated.AllocatedValue;
import org.o42a.codegen.code.backend.AllocatorWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


final class AllocationsMap {

	private static final ID PHIS_ID = ID.rawId("__PHIs__");
	private static final ID ENTRY_ID = ID.rawId("__entry__");
	private static final ID MERGES_ID = ID.rawId("__merges__");
	static final ID ALLOCATIONS_ID = ID.rawId("__allocs__");
	private static final ID ALLOCATOR_DISPOSAL_ID =
			ID.rawId("__allocator_disposal__");

	private final Allocator allocator;
	private final AllocatorWriter writer;
	private final IdentityHashMap<Allocated<?>, AllocRecords> records =
			new IdentityHashMap<>();
	private final IdentityHashMap<Allocator, AllocEntry> entries =
			new IdentityHashMap<>(1);
	private Code phis;
	private Code entry;
	private Code merges;
	private boolean combined;

	AllocationsMap(Allocator allocator, AllocatorWriter writer) {
		this.allocator = allocator;
		this.writer = writer;
	}

	public final Allocator getAllocator() {
		return this.allocator;
	}

	public final Code phis() {
		return this.phis;
	}

	public final Code entry() {
		return this.entry;
	}

	public final Code merges() {
		return this.merges;
	}

	public final boolean isCombined() {
		return this.combined;
	}

	public Code createEntry() {
		this.phis = getAllocator().inset(PHIS_ID);
		this.entry = getAllocator().inset(ENTRY_ID);
		this.merges = getAllocator().inset(MERGES_ID);

		getAllocator().getFunction().addCompleteListener(fn -> combineAll());

		return getAllocator().inset(ALLOCATIONS_ID);
	}

	public void initDisposal() {
		getAllocator().allocate(
				ALLOCATOR_DISPOSAL_ID,
				new AllocatableDisposal(
						code -> disposeAll(code),
						Integer.MIN_VALUE));
	}

	public final <T> AllocatedValue<T> value(
			Code code,
			Allocated<T> allocated) {

		final AllocationMode mode =
				allocated.getAllocatable().getAllocationMode();

		if (!mode.supportsAllocation()) {

			final Code enclosing = code.getAllocator().allocations();

			enclosing.addAsset(
					AllocAsset.class,
					allocatedAsset(enclosing, allocated));

			return null;
		}
		if (this.writer != null && mode.inAllocator()) {
			return new MapAllocatedValue<>(allocated);
		}

		final Code enclosing;

		if (!mode.inAllocator()) {
			enclosing = code;
		} else if (!mode.isNonDebug()) {
			enclosing = code.getAllocator().allocations();
		} else {
			enclosing = code.getAllocator()
					.getNonDebugAllocator()
					.allocations();
		}

		return new Allocations.ImmediateAllocations<>(enclosing, allocated);
	}

	public AllocRecords records(Allocated<?> allocated) {
		assert this.writer != null :
			"Can not record allocations";

		final AllocRecords existing = this.records.get(allocated);

		if (existing != null) {
			return existing;
		}

		final AllocRecords records = new AllocRecords();

		this.records.put(allocated, records);

		return records;
	}

	public AllocEntry entryTo(Allocator target) {
		assert getAllocator().contains(target) :
			target + " is not inside " + getAllocator();
		assert this.writer != null :
			"Can not reallocate";

		final AllocEntry existing = this.entries.get(target);

		if (existing != null) {
			return existing;
		}

		final AllocEntry entry = new AllocEntry(this, target);

		this.entries.put(target, entry);
		if (isCombined()) {
			entry.combineAll();
		}

		return entry;
	}

	public final void allocate(Code code, CodePos target) {
		this.writer.allocate(code, target);
		entryTo(target.code().getAllocator()).allocateIn(code);
	}

	public AnyRecOp allocatePtr(
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id) {

		final AllocRecord<AnyRecOp> record =
				records(allocated).recordPtr(index, id);

		return entryTo(target).dataFor(allocated).ptrsTo(record).ptr();
	}

	public DataRecOp allocateDataPtr(
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id) {

		final AllocRecord<DataRecOp> record =
				records(allocated).recordDataPtr(index, id);

		return entryTo(target).dataFor(allocated).ptrsTo(record).ptr();
	}

	public AnyRecOp reallocatePtr(
			Code code,
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id) {

		final AllocRecord<AnyRecOp> record =
				records(allocated).recordPtr(index, id);

		return entryTo(target).dataFor(allocated).ptrsTo(record).add(code);
	}

	public DataRecOp reallocateDataPtr(
			Code code,
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id) {

		final AllocRecord<DataRecOp> record =
				records(allocated).recordDataPtr(index, id);

		return entryTo(target).dataFor(allocated).ptrsTo(record).add(code);
	}

	public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id,
			Type<S> type) {

		final AllocRecord<StructRecOp<S>> record =
				records(allocated).recordPtr(index, id, type);

		return entryTo(target).dataFor(allocated).ptrsTo(record).ptr();
	}

	public <S extends StructOp<S>> StructRecOp<S> reallocatePtr(
			Code code,
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id,
			Type<S> type) {

		final AllocRecord<StructRecOp<S>> record =
				records(allocated).recordPtr(index, id, type);

		return entryTo(target).dataFor(allocated).ptrsTo(record).add(code);
	}

	public <S extends StructOp<S>> S allocate(
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id,
			Type<S> type) {

		final AllocRecord<S> record =
				records(allocated).recordStruct(index, id, type);

		return entryTo(target).dataFor(allocated).ptrsTo(record).ptr();
	}

	public <S extends StructOp<S>> S reallocate(
			Code code,
			Allocator target,
			Allocated<?> allocated,
			int index,
			ID id,
			Type<S> type) {

		final AllocRecord<S> record =
				records(allocated).recordStruct(index, id, type);

		return entryTo(target).dataFor(allocated).ptrsTo(record).add(code);
	}

	public void combineAll() {
		if (isCombined()) {
			return;
		}
		this.combined = true;
		if (this.writer == null) {
			return;
		}
		this.writer.combine(entry(), entry());
		for (AllocEntry entry :
				this.entries.values().toArray(
						new AllocEntry[this.entries.size()])) {
			entry.combineAll();
		}
		assert checkAllCombined();
	}

	private boolean checkAllCombined() {
		assert isCombined() :
			"Not combined";
		for (AllocEntry entry : this.entries.values()) {
			entry.checkAllCombined();
		}
		return true;
	}

	private void disposeAll(Code code) {
		this.writer.dispose(code, code);
	}

	private final class MapAllocatedValue<T> implements AllocatedValue<T> {

		private final Allocated<T> allocated;

		MapAllocatedValue(Allocated<T> allocated) {
			this.allocated = allocated;
		}

		@Override
		public boolean isAllocated() {
			return true;
		}

		@Override
		public T get(Allocator target) {
			return entryTo(target).dataFor(this.allocated).get();
		}

	}

}
