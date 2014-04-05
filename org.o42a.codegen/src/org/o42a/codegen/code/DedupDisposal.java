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


class DedupDisposal implements InternalDisposal {

	private final Disposal disposal;
	private final Allocator allocator;

	DedupDisposal(Allocator allocator, Disposal disposal) {
		this.allocator = allocator;
		this.disposal = disposal;
		allocator.allocation().addAsset(
				AllocAsset.class,
				allocatedAsset(allocator, this));
	}

	public final Allocator getAllocator() {
		return this.allocator;
	}

	@Override
	public void dispose(Code code) {

		final AllocAsset asset = code.assets().get(AllocAsset.class);

		if (asset == null || !asset.allocated(this)) {
			return;
		}

		this.disposal.dispose(code);
	}

	@Override
	public void afterDispose(Code code) {
		code.addAsset(AllocAsset.class, deallocatedAsset(code, this));
	}

	@Override
	public String toString() {
		if (this.disposal == null) {
			return super.toString();
		}
		return this.disposal.toString();
	}

}
