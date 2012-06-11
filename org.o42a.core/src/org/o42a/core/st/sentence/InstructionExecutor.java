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
package org.o42a.core.st.sentence;

import java.util.List;

import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Implication;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionContext;


final class InstructionExecutor implements InstructionContext {

	private final Statements<?, ?> statements;
	private final Resolver resolver;
	private int index;
	private Implication<?> implication;
	private Block<?, ?> block;
	private boolean doNotRemove;

	InstructionExecutor(Statements<?, ?> statements) {
		this.statements = statements;
		this.resolver = statements.getScope().resolver();
	}

	@Override
	public final Resolver getResolver() {
		return this.resolver;
	}

	@Override
	public Block<?, ?> getBlock() {
		if (this.block != null) {
			return this.block;
		}

		this.doNotRemove = true;

		return this.block = this.statements.parentheses(
				this.index,
				this.implication,
				this.implication.distribute());
	}

	@Override
	public void doNotRemove() {
		this.doNotRemove = true;
	}

	@Override
	public String toString() {
		return "InstructionContext[" + this.implication + ']';
	}

	final void executeAll() {

		final List<? extends Implication<?>> implications =
				this.statements.getImplications();

		while (this.index < implications.size()) {
			execute(implications.get(this.index));
		}
	}

	private final void execute(Implication<?> implication) {

		final Instruction instruction =
				implication.toInstruction(getResolver());

		if (instruction == null) {
			++this.index;
			return;
		}

		this.implication = implication;
		try {
			instruction.execute(this);
			if (!this.doNotRemove) {
				this.statements.removeStatement(this.index);
			} else {
				++this.index;
			}
		} finally {
			this.block = null;
			this.doNotRemove = false;
		}
	}

}
