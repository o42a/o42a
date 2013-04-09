/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.member.clause.impl;


import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.Member;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Block;


public final class ClauseDeclarationStatement extends DeclarationStatement {

	private final Clause clause;
	private final Block<?> definition;

	public ClauseDeclarationStatement(
			ClauseBuilder builder,
			Clause clause,
			Block<?> definition) {
		super(builder, builder.distribute());
		this.clause = clause;
		this.definition = definition;
	}

	@Override
	public Member toMember() {
		return this.clause.toMember();
	}

	public Clause getClause() {
		return this.clause;
	}

	@Override
	public Definer define(CommandEnv env) {
		return new ClauseDefiner(this, env);
	}

	@Override
	public Command command(CommandEnv env) {
		return new ClauseCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		reproducer.applyClause(this, getClause());
		return null;
	}

	final Block<?> getDefinition() {
		return this.definition;
	}

}
