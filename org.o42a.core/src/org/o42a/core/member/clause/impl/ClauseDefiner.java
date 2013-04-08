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

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.member.DeclarationDefiner;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


final class ClauseDefiner extends Definer {

	private Definer definer;

	ClauseDefiner(
			ClauseDeclarationStatement statement,
			CommandEnv env) {
		super(statement, env);
	}

	@Override
	public CommandTargets getTargets() {
		return definer().getTargets();
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return definer().typeParameters(scope);
	}

	@Override
	public Action action(Resolver resolver) {
		return definer().action(resolver);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return definer().toInstruction(resolver);
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return definer().toTarget(origin);
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		definer().resolveTargets(resolver, origin);
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return definer().inline(normalizer, origin);
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return definer().normalize(normalizer, origin);
	}

	@Override
	public Eval eval(Scope origin) {
		assert getStatement().assertFullyResolved();
		return definer().eval(origin);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		definer().resolveAll(resolver);
	}

	private final Definer definer() {
		if (this.definer != null) {
			return this.definer;
		}

		final ClauseDeclarationStatement declaration = declaration();
		final Clause clause = declaration.getClause();

		if (clause.isTopLevel()) {
			return this.definer =
					new ClauseDeclarationDefiner(declaration, env());
		}

		switch (clause.getKind()) {
		case EXPRESSION:
			return new ExpressionClauseDefiner(declaration, env());
		case OVERRIDER:
			return new OverriderClauseDefiner(declaration, env());
		case GROUP:
			return new GroupClauseDefiner(declaration, env());
		}

		throw new IllegalStateException(
				"Unknown kind of clause: " + clause.getKind());
	}

	private final ClauseDeclarationStatement declaration() {
		return (ClauseDeclarationStatement) getStatement();
	}

	private static final class ClauseDeclarationDefiner
			extends DeclarationDefiner {

		ClauseDeclarationDefiner(
				ClauseDeclarationStatement statement,
				CommandEnv env) {
			super(statement, env);
		}

		@Override
		public CommandTargets getTargets() {
			return clauseDef();
		}

	}

	private static final class ExpressionClauseDefiner extends Definer {

		ExpressionClauseDefiner(
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
		public InlineEval inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Eval eval(Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(FullResolver resolver) {
		}

	}

	private static final class OverriderClauseDefiner extends Definer {

		OverriderClauseDefiner(
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
		public InlineEval inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Eval eval(Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(FullResolver resolver) {
		}

	}

	private static final class GroupClauseDefiner extends Definer {

		private final Definer definer;

		GroupClauseDefiner(
				ClauseDeclarationStatement statement,
				CommandEnv env) {
			super(statement, env);
			this.definer = declaration().getDefinition().define(env());
		}

		@Override
		public CommandTargets getTargets() {
			return this.definer.getTargets();
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
		public InlineEval inline(Normalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Eval eval(Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(FullResolver resolver) {
		}

		private final ClauseDeclarationStatement declaration() {
			return (ClauseDeclarationStatement) getStatement();
		}

	}

}
