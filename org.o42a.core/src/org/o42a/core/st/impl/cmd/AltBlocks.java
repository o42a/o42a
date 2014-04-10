/*
    Compiler Core
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
package org.o42a.core.st.impl.cmd;

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.cmd.Control;
import org.o42a.util.string.ID;


class AltBlocks {

	private final Control control;
	private final ID prefix;
	private final Block[] blocks;

	AltBlocks(Control control, ID prefix, int size) {
		this.control = control;
		this.prefix = prefix;
		this.blocks = new Block[size];
	}

	public Block get(int index) {

		final Block existing = this.blocks[index];

		if (existing != null) {
			return existing;
		}

		final Block block =
				this.control.addBlock(this.prefix.sub(index + "_alt"));

		this.blocks[index] = block;

		return block;
	}

	public final int size() {
		return this.blocks.length;
	}

}
