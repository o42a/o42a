/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.code.backend.BlockWriter;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


final class CodeBlock extends Block {

	private final Init<BlockWriter> writer =
			init(() -> getEnclosing().getBlock().writer().block(this));

	CodeBlock(Code enclosing, ID name) {
		super(enclosing, name);
	}

	@Override
	public final Allocator getClosestAllocator() {
		return getEnclosing().getClosestAllocator();
	}

	@Override
	public boolean created() {
		return this.writer.isInitialized() && this.writer.get().created();
	}

	@Override
	public final boolean exists() {
		return this.writer.isInitialized() && this.writer.get().exists();
	}

	@Override
	public BlockWriter writer() {
		return this.writer.get();
	}

}
