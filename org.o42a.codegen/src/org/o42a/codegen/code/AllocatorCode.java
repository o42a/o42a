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

import org.o42a.codegen.code.backend.AllocatorWriter;
import org.o42a.util.string.ID;


final class AllocatorCode extends Allocator {

	private final AllocatorWriter writer;
	private final Allocator enclosingAllocator;

	AllocatorCode(Block enclosing, ID name) {
		super(enclosing, name);
		this.enclosingAllocator = enclosing.getAllocator();
		this.writer = enclosing.writer().allocator(this);
		allocation();
	}

	@Override
	public final Allocator getEnclosingAllocator() {
		return this.enclosingAllocator;
	}

	@Override
	public boolean created() {
		return writer().created();
	}

	@Override
	public final boolean exists() {
		return writer().exists();
	}

	@Override
	public final AllocatorWriter writer() {
		return this.writer;
	}

}
