/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Sentence;


public abstract class BlockDefiner<B extends Block<?>> extends Definer {

	private DefinitionTargets definitionTargets;

	public BlockDefiner(B block, StatementEnv env) {
		super(block, env);
	}

	@SuppressWarnings("unchecked")
	public final B getBlock() {
		return (B) getStatement();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions();
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		if (this.definitionTargets != null) {
			return this.definitionTargets;
		}
		getBlock().executeInstructions();

		DefinitionTargets result = noDefinitions();

		for (Sentence<?> sentence : getBlock().getSentences()) {
			result = result.add(sentence.getDefinitionTargets());
		}

		return this.definitionTargets = result;
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
