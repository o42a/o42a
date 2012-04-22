/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;


final class ClauseCommand extends Command {

	private Command command;

	ClauseCommand(
			ClauseDeclarationStatement statement,
			CommandEnv env) {
		super(statement, env);
	}

	@Override
	public CommandTargets getCommandTargets() {
		return command().getCommandTargets();
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return command().initialValue(resolver);
	}

	@Override
	public Action initialCond(LocalResolver resolver) {
		return command().initialCond(resolver);
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return command().inline(normalizer, origin);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		command().normalize(normalizer);
	}

	@Override
	protected void fullyResolve(LocalResolver resolver) {
		command().resolveAll(resolver);
	}

	@Override
	protected Cmd createCmd(CodeBuilder builder) {
		return command().cmd(builder);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return command().toInstruction(resolver);
	}

	@Override
	public DefTarget toTarget() {
		return command().toTarget();
	}

	private final ClauseDeclarationStatement declaration() {
		return (ClauseDeclarationStatement) getStatement();
	}

	private final Command command() {
		if (this.command != null) {
			return this.command;
		}

		final ClauseDeclarationStatement declaration = declaration();
		final Clause clause = declaration.getClause();

		assert !clause.isTopLevel() :
			"Top-level clause can not be a command" + clause;

		switch (clause.getKind()) {
		case EXPRESSION:
			return new ExpressionClauseCommand(declaration, env());
		case OVERRIDER:
			throw new IllegalArgumentException(
					"Overrider clause could not appear"
					+ " in imperative statement");
		case GROUP:
			return new GroupClauseCommand(declaration, env());
		}

		throw new IllegalStateException(
				"Unknown kind of clause: " + clause.getKind());
	}

	private static final class ExpressionClauseCommand extends Command {

		ExpressionClauseCommand(
				ClauseDeclarationStatement statement,
				CommandEnv env) {
			super(statement, env);
		}

		@Override
		public CommandTargets getCommandTargets() {
			return actionCommand();
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Action initialCond(LocalResolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCmd inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void normalize(RootNormalizer normalizer) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(LocalResolver resolver) {
		}

		@Override
		protected Cmd createCmd(CodeBuilder builder) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public DefTarget toTarget() {
			return DefTarget.NO_DEF_TARGET;
		}

	}

	private static final class GroupClauseCommand extends Command {

		private final Command command;

		GroupClauseCommand(
				ClauseDeclarationStatement statement,
				CommandEnv env) {
			super(statement, env);
			this.command = declaration().getDefinition().command(env());
		}

		@Override
		public CommandTargets getCommandTargets() {
			return this.command.getCommandTargets();
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Action initialCond(LocalResolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCmd inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void normalize(RootNormalizer normalizer) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(LocalResolver resolver) {
			this.command.resolveAll(resolver);
		}

		@Override
		protected Cmd createCmd(CodeBuilder builder) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public DefTarget toTarget() {
			return DefTarget.NO_DEF_TARGET;
		}

		private final ClauseDeclarationStatement declaration() {
			return (ClauseDeclarationStatement) getStatement();
		}

	}

}
