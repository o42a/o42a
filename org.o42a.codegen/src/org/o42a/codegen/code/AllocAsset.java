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

import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.Map;


final class AllocAsset implements CodeAsset<AllocAsset> {

	static AllocAsset allocatedAsset(Code code, Object disposal) {
		return new AllocAsset(disposal, new Alloc(code, true));
	}

	static AllocAsset deallocatedAsset(Code code, Object disposal) {
		return new AllocAsset(disposal, new Alloc(code, false));
	}

	private final Map<Object, Alloc> allocs;

	private AllocAsset(Object disposal, Alloc alloc) {
		this.allocs = singletonMap(disposal, alloc);
	}

	private AllocAsset(Map<Object, Alloc> disposals) {
		this.allocs = disposals;
	}

	public final boolean allocated(Object disposal) {

		final Alloc alloc = this.allocs.get(disposal);

		if (alloc == null) {
			return false;
		}

		assert !alloc.isConflict() :
			alloc;

		return alloc.isAllocated();
	}

	@Override
	public AllocAsset combineWith(AllocAsset asset) {
		if (asset.allocs.isEmpty()) {
			return this;
		}
		if (this.allocs.isEmpty()) {
			return asset;
		}

		final HashMap<Object, Alloc> allocs =
				new HashMap<>(this.allocs.size() + asset.allocs.size());

		allocs.putAll(this.allocs);

		for (Map.Entry<Object, Alloc> e : asset.allocs.entrySet()) {

			final Object key = e.getKey();
			final Alloc alloc = e.getValue();
			final Alloc prevAlloc = allocs.get(key);

			if (prevAlloc == null) {
				allocs.put(key, alloc);
			} else {
				allocs.put(key, prevAlloc.combineWith(alloc));
			}
		}

		return new AllocAsset(allocs);
	}

	@Override
	public AllocAsset overwriteBy(AllocAsset asset) {
		if (asset.allocs.isEmpty()) {
			return this;
		}
		if (this.allocs.isEmpty()) {
			return asset;
		}

		final HashMap<Object, Alloc> allocs =
				new HashMap<>(this.allocs.size() + asset.allocs.size());

		allocs.putAll(this.allocs);
		allocs.putAll(asset.allocs);

		return new AllocAsset(allocs);
	}

	@Override
	public String toString() {
		if (this.allocs == null) {
			return super.toString();
		}
		return "AllocAssets" + this.allocs;
	}

	private static class Alloc {

		private final Code code;
		private final boolean allocated;

		Alloc(Code code, boolean allocated) {
			this.code = code;
			this.allocated = allocated;
		}

		public boolean isConflict() {
			return false;
		}

		public final boolean isAllocated() {
			return this.allocated;
		}

		public Alloc combineWith(Alloc other) {
			if (other.isConflict()) {
				return other;
			}
			if (isAllocated() == other.isAllocated()) {
				return this;
			}
			return new AllocConflict(this, other);
		}

		@Override
		public String toString() {
			if (this.code == null) {
				return super.toString();
			}
			if (this.allocated) {
				return "Allocated@" + this.code;
			}
			return "Deallocated@" + this.code;
		}

	}

	private static class AllocConflict extends Alloc {

		private final Alloc alloc1;
		private final Alloc alloc2;

		AllocConflict(Alloc alloc1, Alloc alloc2) {
			super(alloc1.code, true);
			this.alloc1 = alloc1;
			this.alloc2 = alloc2;
		}

		@Override
		public boolean isConflict() {
			return true;
		}

		@Override
		public Alloc combineWith(Alloc other) {
			return this;
		}

		@Override
		public String toString() {
			if (this.alloc2 == null) {
				return super.toString();
			}
			return "AllocConflict[" + this.alloc1 + ", " + this.alloc2 +']';
		}

	}

}
