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

import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.util.string.ID;


final class AllocatorCode extends Allocator {

	private final Allocator enclosingAllocator;
	private final BlockWriter writer;
	private final Disposal disposal;

	AllocatorCode(Block enclosing, ID name) {
		super(enclosing, name);
		this.enclosingAllocator = enclosing.getAllocator();
		this.writer = enclosing.writer().block(this);
		this.disposal = enclosing.writer().startAllocation(this);
		enclosing.writer().go(unwrapPos(head()));
		allocation();
	}

	@Override
	public final Allocator getEnclosingAllocator() {
		return this.enclosingAllocator;
	}

	@Override
	public final boolean created() {
		return writer().created();
	}

	@Override
	public final boolean exists() {
		return writer().exists();
	}

	@Override
	public final BlockWriter writer() {
		return this.writer;
	}

	@Override
	protected Disposal disposal() {
		return this.disposal;
	}

}
