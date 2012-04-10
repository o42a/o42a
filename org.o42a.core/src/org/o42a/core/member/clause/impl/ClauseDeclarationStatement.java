/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.Block;


public final class ClauseDeclarationStatement extends DeclarationStatement {

	private final Clause clause;
	private final Block<?, ?> definition;
	private boolean defined;

	public ClauseDeclarationStatement(
			ClauseBuilder builder,
			Clause clause,
			Block<?, ?> definition) {
		super(builder, builder.distribute());
		this.clause = clause;
		this.definition = definition;
		this.defined = definition == null;
	}

	@Override
	public Member toMember() {
		return this.clause.toMember();
	}

	public Clause getClause() {
		return this.clause;
	}

	@Override
	public DeclarationDefiner define(DefinerEnv env) {
		if (!this.defined) {
			this.defined = true;
			this.definition.define(env);
		}
		return new Definer(this, env);
	}

	@Override
	public DeclarationCommand command(CommandEnv env) {
		if (!this.defined) {
			this.defined = true;
			this.definition.command(env);
		}
		return new Command(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		reproducer.applyClause(this, getClause());
		return null;
	}

	private static final class Definer extends DeclarationDefiner {

		Definer(ClauseDeclarationStatement statement, DefinerEnv env) {
			super(statement, env);
		}

		@Override
		public DefinerEnv nextEnv() {
			return env().notCondition(this);
		}

		@Override
		public DefinitionTargets getDefinitionTargets() {
			return noDefinitions();
		}

	}

	private static final class Command extends DeclarationCommand {

		Command(ClauseDeclarationStatement statement, CommandEnv env) {
			super(statement, env);
		}

		@Override
		public DefinitionTargets getDefinitionTargets() {
			return noDefinitions();
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			return null;
		}

		@Override
		protected Cmd createCmd(CodeBuilder builder) {
			return null;
		}

	}

}
