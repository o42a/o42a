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

	private final Map<DedupDisposal, Boolean> disposals;

	AllocAsset(DedupDisposal disposal) {
		this.disposals = singletonMap(disposal, Boolean.TRUE);
	}

	private AllocAsset(Map<DedupDisposal, Boolean> disposals) {
		this.disposals = disposals;
	}

	public final boolean allocated(DedupDisposal disposal) {

		final Boolean allocated = this.disposals.get(disposal);

		return allocated != null && allocated.booleanValue();
	}

	public final AllocAsset deallocate(DedupDisposal disposal) {

		final HashMap<DedupDisposal, Boolean> disposals =
				new HashMap<>(this.disposals.size());

		disposals.putAll(this.disposals);
		disposals.put(disposal, Boolean.FALSE);

		return new AllocAsset(disposals);
	}

	@Override
	public AllocAsset add(AllocAsset asset) {
		if (asset.disposals.isEmpty()) {
			return this;
		}
		if (this.disposals.isEmpty()) {
			return asset;
		}

		final HashMap<DedupDisposal, Boolean> disposals =
				new HashMap<>(this.disposals.size() + asset.disposals.size());

		disposals.putAll(this.disposals);

		for (Map.Entry<DedupDisposal, Boolean> e : asset.disposals.entrySet()) {

			final DedupDisposal key = e.getKey();
			final Boolean value = e.getValue();

			if (value) {

				final Boolean oldValue = disposals.get(key);

				if (oldValue != null && !oldValue.booleanValue()) {
					// Deallocation has precedence.
					continue;
				}
			}

			disposals.put(key, value);
		}

		return new AllocAsset(disposals);
	}

}
