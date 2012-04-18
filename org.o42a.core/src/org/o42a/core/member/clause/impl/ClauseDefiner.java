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

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.member.DeclarationDefiner;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueStruct;


final class ClauseDefiner extends Definer {

	private Definer definer;

	ClauseDefiner(
			ClauseDeclarationStatement statement,
			DefinerEnv env) {
		super(statement, env);
	}

	@Override
	public DefTargets getDefTargets() {
		return definer().getDefTargets();
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return definer().getDefinitionTargets();
	}

	@Override
	public DefinerEnv nextEnv() {
		return env().notCondition(this);
	}

	@Override
	public Definitions define(Scope scope) {
		return definer().define(scope);
	}

	@Override
	public DefValue value(Resolver resolver) {
		return definer().value(resolver);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return definer().toInstruction(resolver);
	}

	@Override
	public InlineEval inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		return definer().inline(normalizer, valueStruct, origin);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		definer().normalize(normalizer);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		definer().resolveAll(resolver);
	}

	@Override
	protected Eval createEval(CodeBuilder builder) {
		return definer().eval(builder);
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
				DefinerEnv env) {
			super(statement, env);
		}

		@Override
		public DefTargets getDefTargets() {
			return clauseDef();
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

	private static final class ExpressionClauseDefiner extends Definer {

		ExpressionClauseDefiner(
				ClauseDeclarationStatement statement,
				DefinerEnv env) {
			super(statement, env);
		}

		@Override
		public DefTargets getDefTargets() {
			return expressionDef();
		}

		@Override
		public DefinitionTargets getDefinitionTargets() {
			return noDefinitions();
		}

		@Override
		public DefinerEnv nextEnv() {
			return env().notCondition(this);
		}

		@Override
		public Definitions define(Scope scope) {
			throw new UnsupportedOperationException();
		}

		@Override
		public DefValue value(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public InlineEval inline(
				Normalizer normalizer,
				ValueStruct<?, ?> valueStruct,
				Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void normalize(RootNormalizer normalizer) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(Resolver resolver) {
		}

		@Override
		protected Eval createEval(CodeBuilder builder) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class OverriderClauseDefiner extends Definer {

		OverriderClauseDefiner(
				ClauseDeclarationStatement statement,
				DefinerEnv env) {
			super(statement, env);
		}

		@Override
		public DefTargets getDefTargets() {
			return fieldDef();
		}

		@Override
		public DefinitionTargets getDefinitionTargets() {
			return noDefinitions();
		}

		@Override
		public DefinerEnv nextEnv() {
			return env().notCondition(this);
		}

		@Override
		public Definitions define(Scope scope) {
			throw new UnsupportedOperationException();
		}

		@Override
		public DefValue value(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public InlineEval inline(
				Normalizer normalizer,
				ValueStruct<?, ?> valueStruct,
				Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void normalize(RootNormalizer normalizer) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(Resolver resolver) {
		}

		@Override
		protected Eval createEval(CodeBuilder builder) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class GroupClauseDefiner extends Definer {

		private final Definer definer;

		GroupClauseDefiner(
				ClauseDeclarationStatement statement,
				DefinerEnv env) {
			super(statement, env);
			this.definer = declaration().getDefinition().define(env());
		}

		@Override
		public DefTargets getDefTargets() {
			return this.definer.getDefTargets();
		}

		@Override
		public DefinitionTargets getDefinitionTargets() {
			return noDefinitions();
		}

		@Override
		public DefinerEnv nextEnv() {
			return env().notCondition(this);
		}

		@Override
		public Definitions define(Scope scope) {
			throw new UnsupportedOperationException();
		}

		@Override
		public DefValue value(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instruction toInstruction(Resolver resolver) {
			return null;
		}

		@Override
		public InlineEval inline(
				Normalizer normalizer,
				ValueStruct<?, ?> valueStruct,
				Scope origin) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void normalize(RootNormalizer normalizer) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(Resolver resolver) {
		}

		@Override
		protected Eval createEval(CodeBuilder builder) {
			throw new UnsupportedOperationException();
		}

		private final ClauseDeclarationStatement declaration() {
			return (ClauseDeclarationStatement) getStatement();
		}

	}

}
