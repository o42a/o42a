/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.util.fn.Init.init;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.member.DeclarationCommand;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Init;


final class ClauseCommand extends Command {

	private final Init<Command> command = init(this::buildCommand);

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
	public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
		return command().escapeFlag(analyzer, scope);
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		command().resolveTargets(resolver, origin);
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return command().inline(normalizer, origin);
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return command().normalize(normalizer, origin);
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
		return this.command.get();
	}

	private Command buildCommand() {

		final ClauseDeclarationStatement declaration = declaration();
		final Clause clause = declaration.getClause();

		if (clause.isTopLevel()) {
			return new ClauseDeclarationCommand(declaration, env());
		}

		switch (clause.getKind()) {
		case EXPRESSION:
			return new ExpressionClauseCommand(declaration, env());
		case OVERRIDER:
			return new OverriderClauseCommand(declaration, env());
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
		public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
			return analyzer.escapeImpossible();
		}

		@Override
		public void resolveTargets(TargetResolver resolver, Scope origin) {
		}

		@Override
		public InlineCmd inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
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

	private static final class OverriderClauseCommand extends Command {

		OverriderClauseCommand(
				ClauseDeclarationStatement statement,
				CommandEnv env) {
			super(statement, env);
		}

		@Override
		public CommandTargets getTargets() {
			return fieldDef();
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
		public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
			return analyzer.escapeImpossible();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public DefTarget toTarget(Scope origin) {
			return NO_DEF_TARGET;
		}

		@Override
		public void resolveTargets(TargetResolver resolver, Scope origin) {
		}

		@Override
		public InlineCmd inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Cmd cmd(Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(FullResolver resolver) {
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
		public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
			return analyzer.escapeImpossible();
		}

		@Override
		public void resolveTargets(TargetResolver resolver, Scope origin) {
		}

		@Override
		public InlineCmd inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
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
