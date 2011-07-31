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
package org.o42a.core.st.sentence;

import java.util.List;

import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.Statement;
import org.o42a.util.use.*;


final class InstructionExecutor implements InstructionContext {

	private final Statements<?> statements;
	private final Resolver resolver;
	private int index;
	private Statement statement;
	private Block<?> block;
	private boolean doNotRemove;

	InstructionExecutor(Statements<?> statements) {
		this.statements = statements;
		this.resolver = statements.getScope().dummyResolver();
	}

	@Override
	public final User toUser() {
		return getResolver().toUser();
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		return toUser().getUseBy(useCase);
	}

	@Override
	public boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public final Resolver getResolver() {
		return this.resolver;
	}

	@Override
	public Block<?> getBlock() {
		if (this.block != null) {
			return this.block;
		}

		this.doNotRemove = true;

		return this.block = this.statements.parentheses(
				this.index,
				this.statement,
				this.statement.distribute(),
				this.statements.getMemberRegistry());
	}

	@Override
	public void doNotRemove() {
		this.doNotRemove = true;
	}

	@Override
	public String toString() {
		return "InstructionContext[" + this.statement + ']';
	}

	final void executeAll() {

		final List<Statement> statements = this.statements.getStatements();

		while (this.index < statements.size()) {
			execute(statements.get(this.index));
		}
	}

	private final void execute(Statement statement) {

		final Instruction instruction =
			statement.toInstruction(getResolver());

		if (instruction == null) {
			++this.index;
			return;
		}

		this.statement = statement;
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
