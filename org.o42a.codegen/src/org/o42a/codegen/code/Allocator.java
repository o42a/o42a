/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import static org.o42a.codegen.code.AllocationsMap.ALLOCATIONS_ID;
import static org.o42a.codegen.data.AllocPlace.autoAllocPlace;

import java.util.HashMap;
import java.util.TreeSet;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.AllocatorWriter;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public abstract class Allocator extends Block {

	private static final ID DISPOSAL_ID = ID.rawId("__disposal__");
	private static final ID DISPOSE_ID = ID.rawId("__dispose__");

	private final AllocPlace allocPlace = autoAllocPlace(this);
	private final TreeSet<Allocated<?>> allocated = new TreeSet<>();
	private AllocationsMap allocationsMap;
	private Code allocations;
	private HashMap<Class<?>, Object> data;
	private final boolean debugAllocator;

	Allocator(Block enclosing, ID name, boolean debugAllocator) {
		super(enclosing, name);
		this.debugAllocator = debugAllocator;
	}

	Allocator(Generator generator, ID id) {
		super(generator, id);
		this.debugAllocator = false;
	}

	public final AllocPlace getAllocPlace() {
		return this.allocPlace;
	}

	public final void addDisposal(Disposal disposal) {
		allocate(DISPOSAL_ID, new AllocatableDisposal(disposal));
	}

	public final boolean isDebugAllocator() {
		return this.debugAllocator;
	}

	@Override
	public final Allocator getAllocator() {
		if (!isDebugAllocator()) {
			return this;
		}
		return getEnclosingAllocator().getAllocator();
	}

	@Override
	public final Allocator getClosestAllocator() {
		return this;
	}

	public abstract Allocator getEnclosingAllocator();

	public final Code allocations() {
		assert this.allocations != null :
			"Allocations not started yet";
		return this.allocations;
	}

	public final <T> T get(Class<? extends T> klass) {

		final T found = find(klass);

		if (found != null) {
			return found;
		}

		final Allocator enclosing = getEnclosingAllocator();

		if (enclosing == null) {
			return null;
		}

		return enclosing.get(klass);
	}

	public final <T> void put(Class<? extends T> klass, T value) {
		if (this.data == null) {
			this.data = new HashMap<>(1);
		}
		this.data.put(klass, value);
	}

	final AllocationsMap allocationsMap() {
		assert this.allocationsMap != null :
			"Allocator not initialized yet: " + this;
		return this.allocationsMap;
	}

	final void initAllocations(final AllocatorWriter allocatorWriter) {
		assert this.allocations == null :
			"Allocation already started";

		if (allocatorWriter == null) {
			this.allocationsMap = new AllocationsMap(this, allocatorWriter);
			this.allocations = inset(ALLOCATIONS_ID);
		} else {
			this.allocationsMap = new AllocationsMap(this, allocatorWriter);
			this.allocations = this.allocationsMap.createEntry();
			this.allocationsMap.initDisposal();
		}
	}

	final void allocate(Code code, CodePos target) {
		this.allocationsMap.allocate(code, target);
	}

	final void dispose(Code code) {
		getFunction().addCompleteListener(new Disposer(this, code));
		afterDispose(code);
	}

	final <T> Allocated<T> addAllocation(ID id, Allocatable<T> allocatable) {

		final Allocated<T> allocated =
				new Allocated<>(this, id, allocatable, this.allocated.size());

		this.allocated.add(allocated);

		return allocated;
	}

	private <T> T find(Class<? extends T> klass) {
		if (this.data == null) {
			return null;
		}
		return klass.cast(this.data.get(klass));
	}

	private void disposeIn(Code code) {
		for (Allocated<?> allocated : this.allocated) {
			allocated.dispose(code);
		}
		// Disposals may request new pointers and generate new entries.
		// They need to be combined too.
		this.allocationsMap.combineAll();
	}

	private void afterDispose(Code code) {
		for (Allocated<?> allocated : this.allocated) {
			allocated.afterDispose(code);
		}
	}

	private static final class Disposer
			implements FunctionCompleteListener {

		private final Allocator allocator;
		private final Code dispose;

		Disposer(Allocator allocator, Code code) {
			this.allocator = allocator;
			this.dispose = code.inset(DISPOSE_ID);
		}

		@Override
		public void functionComplete(Function<?> function) {
			this.allocator.disposeIn(this.dispose);
		}

	}

}
