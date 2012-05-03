/*
    Compiler Code Generator
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.AllocatorWriter;


public abstract class Allocator extends Block {

	private AllocationCode allocation;

	Allocator(Block enclosing, CodeId name) {
		super(enclosing, name);
	}

	Allocator(Generator generator, CodeId id) {
		super(generator, id);
	}

	@Override
	public final Allocator getAllocator() {
		return this;
	}

	public abstract Allocator getEnclosingAllocator();

	public final AllocationCode allocation() {
		return this.allocation;
	}

	@Override
	public abstract AllocatorWriter writer();

	protected final void initAllocator() {
		this.allocation = new AllocationCode(this);
	}

}
