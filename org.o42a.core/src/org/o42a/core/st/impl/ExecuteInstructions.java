/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.st.impl;

import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.Block;


public final class ExecuteInstructions implements Instruction {

	private final Block<?> block;

	public ExecuteInstructions(Block<?> block) {
		this.block = block;
	}

	@Override
	public void execute(InstructionContext context) {
		context.doNotRemove();
		this.block.executeInstructions();
	}

	@Override
	public String toString() {
		return "ExecuteInstructions[" + this.block + ']';
	}

}
