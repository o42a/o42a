/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Block;


public abstract class BlockImplication<
		B extends Block<?, ?>,
		L extends Implication<L>>
				extends AbstractImplication<L> {

	public BlockImplication(B block) {
		super(block);
	}

	@SuppressWarnings("unchecked")
	public final B getBlock() {
		return (B) getStatement();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions();
	}

	private final class ExecuteInstructions implements Instruction {

		@Override
		public void execute(InstructionContext context) {
			context.doNotRemove();
			getBlock().executeInstructions();
		}

		@Override
		public String toString() {
			return "ExecuteInstructions[" + getBlock() + ']';
		}

	}

}
