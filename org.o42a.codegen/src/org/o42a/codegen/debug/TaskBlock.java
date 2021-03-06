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
package org.o42a.codegen.debug;

import org.o42a.codegen.code.Block;


public final class TaskBlock {

	private final Block enclosing;
	private final Block block;

	TaskBlock(Block enclosing, Block block) {
		this.enclosing = enclosing;
		this.block = block;
	}

	public final Block code() {
		return this.block;
	}

	public final Block end() {
		if (code().getGenerator().getDebug().isDebugBlocksOmitted()) {
			return this.block;
		}

		if (this.block.exists()) {
			this.block.go(this.enclosing.tail());
		}

		return this.enclosing;
	}

}
