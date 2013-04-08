/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.member.DeclarationCommand;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


final class ClauseCommand extends Command {

	private Command command;

	ClauseCommand(
			ClauseDeclarationStatement statement,
			CommandEnv env) {
		super(statement, env);
	}

	@Override
	public CommandTargets getTargets() {
		return command().getTargets();
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return command().typeParameters(scope);
	}

	@Override
	public Action action(Resolver resolver) {
		return command().action(resolver);
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		command().resolveTargets(resolver, origin);
	}

	@Override
	public InlineCmd inlineCmd(Normalizer normalizer, Scope origin) {
		return command().inlineCmd(normalizer, origin);
	}

	@Override
	public InlineCmd normalizeCmd(RootNormalizer normalizer, Scope origin) {
		return command().normalizeCmd(normalizer, origin);
	}

	@Override
	public Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return command().cmd(origin);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		command().resolveAll(resolver);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return command().toInstruction(resolver);
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return command().toTarget(origin);
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

		if (clause.isTopLevel()) {
			return this.command =
					new ClauseDeclarationCommand(declaration, env());
		}

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

	private static final class ClauseDeclarationCommand
			extends DeclarationCommand {

		ClauseDeclarationCommand(
				ClauseDeclarationStatement statement,
				CommandEnv env) {
			super(statement, env);
		}

		@Override
		public CommandTargets getTargets() {
			return clauseDef();
		}

	}

	private static final class ExpressionClauseCommand extends Command {

		ExpressionClauseCommand(
				ClauseDeclarationStatement statement,
				CommandEnv env) {
			super(statement, env);
		}

		@Override
		public CommandTargets getTargets() {
			return actionCommand();
		}

		@Override
		public TypeParameters<?> typeParameters(Scope scope) {
			return null;
		}

		@Override
		public Action action(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void resolveTargets(TargetResolver resolver, Scope origin) {
		}

		@Override
		public InlineCmd inlineCmd(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCmd normalizeCmd(RootNormalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Cmd cmd(Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(FullResolver resolver) {
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public DefTarget toTarget(Scope origin) {
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
		public CommandTargets getTargets() {
			return this.command.getTargets();
		}

		@Override
		public TypeParameters<?> typeParameters(Scope scope) {
			return null;
		}

		@Override
		public Action action(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void resolveTargets(TargetResolver resolver, Scope origin) {
		}

		@Override
		public InlineCmd inlineCmd(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCmd normalizeCmd(RootNormalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Cmd cmd(Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(FullResolver resolver) {
			this.command.resolveAll(resolver);
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public DefTarget toTarget(Scope origin) {
			return DefTarget.NO_DEF_TARGET;
		}

		private final ClauseDeclarationStatement declaration() {
			return (ClauseDeclarationStatement) getStatement();
		}

	}

}
