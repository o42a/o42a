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

	static AllocAsset allocatedAsset(Code code, DedupDisposal disposal) {
		return new AllocAsset(disposal, new Alloc(code, true));
	}

	static AllocAsset deallocatedAsset(Code code, DedupDisposal disposal) {
		return new AllocAsset(disposal, new Alloc(code, false));
	}

	private final Map<DedupDisposal, Alloc> allocs;

	private AllocAsset(DedupDisposal disposal, Alloc alloc) {
		this.allocs = singletonMap(disposal, alloc);
	}

	private AllocAsset(Map<DedupDisposal, Alloc> disposals) {
		this.allocs = disposals;
	}

	public final boolean allocated(DedupDisposal disposal) {

		final Alloc alloc = this.allocs.get(disposal);

		return alloc != null && alloc.isAllocated();
	}

	public final AllocAsset deallocate(Code code, DedupDisposal disposal) {

		final Alloc alloc = this.allocs.get(disposal);
		final HashMap<DedupDisposal, Alloc> disposals;

		if (alloc == null) {
			disposals = new HashMap<>(this.allocs.size() + 1);
		} else if (!alloc.isAllocated()) {
			assert alloc.isAllocated() :
				"Double deallocation of " + disposal;
			return this;
		} else {
			disposals = new HashMap<>(this.allocs.size());
		}

		disposals.putAll(this.allocs);
		disposals.put(disposal, new Alloc(code, false));

		return new AllocAsset(disposals);
	}

	@Override
	public AllocAsset combine(AllocAsset asset) {
		if (asset.allocs.isEmpty()) {
			return this;
		}
		if (this.allocs.isEmpty()) {
			return asset;
		}

		final HashMap<DedupDisposal, Alloc> allocs =
				new HashMap<>(this.allocs.size() + asset.allocs.size());

		allocs.putAll(this.allocs);

		for (Map.Entry<DedupDisposal, Alloc> e : asset.allocs.entrySet()) {

			final DedupDisposal key = e.getKey();
			final Alloc alloc = e.getValue();
			final Alloc prevAlloc = allocs.get(key);

			if (prevAlloc != null) {
				// Allocation has precedence over deallocation.
				assert prevAlloc.isAllocated() == alloc.isAllocated() :
					"Allocation/deallocation conflict: " + key;
				if (prevAlloc.isAllocated()) {
					continue;
				}
				if (!alloc.isAllocated()) {
					continue;
				}
			}

			allocs.put(key, alloc);
		}

		return new AllocAsset(allocs);
	}

	@Override
	public String toString() {
		if (this.allocs == null) {
			return super.toString();
		}
		return "AllocAssets" + this.allocs;
	}

	private static final class Alloc {

		private final Code code;
		private final boolean allocated;

		Alloc(Code code, boolean allocated) {
			this.code = code;
			this.allocated = allocated;
		}

		public final boolean isAllocated() {
			return this.allocated;
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

}
