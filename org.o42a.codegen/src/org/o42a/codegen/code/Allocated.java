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
import static org.o42a.codegen.code.AllocAsset.deallocatedAsset;

import org.o42a.util.string.ID;


public class Allocated<T> implements Comparable<Allocated<T>> {

	private final ID id;
	private final Allocatable<T> allocatable;
	private Allocations alloc;
	private T result;
	private final int order;
	private boolean allocated;

	Allocated(ID id, Allocatable<T> allocatable, int order) {
		this.id = id;
		this.allocatable = allocatable;
		this.order = order;
	}

	public final ID getId() {
		return this.id;
	}

	public final Allocatable<T> getAllocatable() {
		return this.allocatable;
	}

	public final boolean isAllocated() {
		return this.allocated;
	}

	public final T get(Code code) {
		if (!this.allocated) {
			allocate();
		}
		return this.result;
	}

	@Override
	public int compareTo(Allocated<T> o) {

		final int cmp = Integer.compare(
				o.getAllocatable().getDisposePriority(),
				getAllocatable().getDisposePriority());

		if (cmp != 0) {
			return cmp;
		}

		return Integer.compare(o.order, this.order);
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	void init(Code code) {

		final AllocationMode mode = getAllocatable().getAllocationMode();

		if (!mode.supportsAllocation()) {

			final Code enclosing = code.getAllocator().allocations();

			enclosing.addAsset(
					AllocAsset.class,
					allocatedAsset(enclosing, this));

			this.allocated = true;
		} else {

			final Code enclosing =
					mode.inAllocator()
					? code.getAllocator().allocations()
					: code;

			this.alloc = new Allocations(enclosing, this);

			if (mode.isMandatory()) {
				allocate();
			}
		}
	}

	void afterDispose(Code code) {
		code.addAsset(AllocAsset.class, deallocatedAsset(code, this));
	}

	void dispose(Code code) {
		if (!isAllocated()) {
			return;
		}

		final AllocAsset asset = code.assets().get(AllocAsset.class);

		if (asset == null || !asset.allocated(this)) {
			return;
		}

		getAllocatable().dispose(code, this);
	}

	private void allocate() {
		this.allocated = true;
		this.result = getAllocatable().allocate(this.alloc, this);
		this.alloc.code().addAsset(
				AllocAsset.class,
				allocatedAsset(this.alloc.code(), this));
		getAllocatable().init(this.alloc.code(), this);
	}

}
