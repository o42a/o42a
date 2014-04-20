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

import java.util.IdentityHashMap;

import org.o42a.codegen.code.backend.AllocatorWriter;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID;


final class AllocationsMap {

	private static final ID ENTRY_ID = ID.rawId("__entry__");
	static final ID ALLOCATIONS_ID = ID.rawId("__allocs__");
	private static final ID ALLOCATOR_DISPOSAL_ID =
			ID.rawId("__allocator_disposal__");

	private final Allocator allocator;
	private final AllocatorWriter writer;
	private final IdentityHashMap<Allocator, Entry> entries =
			new IdentityHashMap<>(1);

	AllocationsMap(Allocator allocator, AllocatorWriter writer) {
		this.allocator = allocator;
		this.writer = writer;
	}

	public Code createEntry() {

		final Code entry = getAllocator().inset(ENTRY_ID);

		getAllocator().getFunction()
		.addCompleteListener(
				new FunctionCompleteListener() {
					@Override
					public void functionComplete(Function<?> function) {
						combine(entry);
					}
				});

		return getAllocator().inset(ALLOCATIONS_ID);
	}

	public void initDisposal() {
		getAllocator().allocate(
				ALLOCATOR_DISPOSAL_ID,
				new AllocatableDisposal(
						new Disposal() {
							@Override
							public void dispose(Code code) {
								disposeAll(code);
							}
						},
						Integer.MIN_VALUE));
	}

	public final Allocator getAllocator() {
		return this.allocator;
	}

	public void allocate(Code code, CodePos target) {
		this.writer.allocate(code, target);
		entry(target.code().getAllocator()).addAlloc(code);
	}

	private void combine(Code code) {
		this.writer.combine(code, code);
		// TODO Auto-generated method stub

	}

	private void disposeAll(Code code) {
		// TODO Auto-generated method stub
		this.writer.dispose(code, code);
	}

	private Entry entry(Allocator target) {

		final Entry existing = this.entries.get(target);

		if (existing != null) {
			return existing;
		}

		final Entry entry= new Entry(target);

		this.entries.put(target, entry);

		return entry;
	}

	private final class Entry {

		private final Allocator target;
		private Code[] allocs;

		Entry(Allocator target) {
			this.target = target;
		}

		Entry parentEntry() {
			if (this.target == getAllocator()) {
				return null;
			}
			return entry(this.target.getEnclosingAllocator());
		}

		void addAlloc(Code code) {
			if (this.allocs == null) {
				this.allocs = new Code[] {code};
			} else {
				this.allocs = ArrayUtil.append(this.allocs, code);
			}
		}

	}

}
