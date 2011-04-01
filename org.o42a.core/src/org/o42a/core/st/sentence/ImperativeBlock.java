/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.ScopePlace.localPlace;
import static org.o42a.util.Place.FIRST_PLACE;

import java.util.List;

import org.o42a.core.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.LocalRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.action.LoopAction;
import org.o42a.core.st.sentence.imperative.BracesWithinDeclaratives;
import org.o42a.core.st.sentence.imperative.ImperativeBlockOp;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueType;
import org.o42a.util.Lambda;
import org.o42a.util.Place.Trace;


public final class ImperativeBlock extends Block<Imperatives> {

	public static ImperativeBlock topLevelImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			String name,
			ImperativeFactory sentenceFactory,
			Lambda<MemberRegistry, LocalScope> memberRegistry) {

		final LocalScope scope =
			enclosing.getMemberRegistry().newLocalScope(
					location,
					distributor,
					name);

		if (scope == null) {
			return null;
		}

		final MemberRegistry registry;

		if (memberRegistry != null) {
			registry = memberRegistry.get(scope);
		} else {
			registry = new LocalRegistry(scope, enclosing.getMemberRegistry());
		}

		return new ImperativeBlock(
				location,
				scope,
				registry,
				sentenceFactory);
	}

	public static ImperativeBlock nestedImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			boolean parentheses,
			String name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		return new ImperativeBlock(
				location,
				distributor,
				enclosing,
				parentheses,
				name,
				memberRegistry,
				sentenceFactory);
	}

	private final boolean parentheses;
	private final String name;
	private final boolean topLevel;
	private final Trace trace;
	private ValueType<?> valueType;
	private StatementEnv initialEnv;

	public ImperativeBlock(
			LocationInfo location,
			LocalScope scope,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		this(
				location,
				new BlockDistributor(scope),
				memberRegistry,
				sentenceFactory);
	}

	private ImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(location, distributor, memberRegistry, sentenceFactory);
		this.parentheses = false;
		this.name = distributor.getScope().toLocal().getName();
		this.topLevel = true;
		this.trace = getPlace().nestedTrace();

		final LocalScopeBase scope = getScope();

		scope.setBlock(this);
	}

	private ImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			boolean parentheses,
			String name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(
				location,
				distributor,
				enclosing,
				memberRegistry,
				sentenceFactory);
		this.parentheses = parentheses;
		this.name = name;
		this.topLevel = false;
		this.trace = getPlace().nestedTrace();
	}

	public final boolean isTopLevel() {
		return this.topLevel;
	}

	@Override
	public LocalScope getScope() {
		return super.getScope().toLocal();
	}

	@Override
	public ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ImperativeSentence> getSentences() {
		return (List<ImperativeSentence>) super.getSentences();
	}

	@Override
	public final boolean isParentheses() {
		return this.parentheses;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public ValueType<?> getValueType() {
		if (this.valueType != null) {
			return this.valueType;
		}
		if (!getDefinitionTargets().haveValue()) {
			return this.valueType = ValueType.VOID;
		}

		ValueType<?> result = null;

		for (Sentence<?> sentence : getSentences()) {

			final ValueType<?> type = sentence.valueType(result);

			if (type == null) {
				continue;
			}
			if (result == null) {
				result = type;
				continue;
			}
			if (result != type) {
				getLogger().incompatible(sentence, result);
			}
		}

		return this.valueType = result;
	}

	@Override
	public final ImperativeBlock toImperativeBlock() {
		return this;
	}

	@Override
	public final ImperativeSentence propose(LocationInfo location) {
		return (ImperativeSentence) super.propose(location);
	}

	@Override
	public final ImperativeSentence claim(LocationInfo location) {
		return (ImperativeSentence) super.claim(location);
	}

	@Override
	public final ImperativeSentence issue(LocationInfo location) {
		return (ImperativeSentence) super.issue(location);
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		assert this.initialEnv == null :
			"Environment already set for " + this;
		this.initialEnv = env;
		return new ImperativeEnv(this, env);
	}

	@Override
	public Definitions define(Scope scope) {
		return this.initialEnv.apply(
				localDef(this, scope.getScope())).toDefinitions();
	}

	@Override
	public Action initialValue(LocalScope scope) {
		for (ImperativeSentence sentence : getSentences()) {

			final Action action = sentence.initialValue(scope);
			final LoopAction loopAction = action.toLoopAction(this);

			switch (loopAction) {
			case CONTINUE:
				continue;
			case PULL:
				return action;
			case EXIT:
				return new ExecuteCommand(action, action.getLogicalValue());
			case REPEAT:
				// Repeating is not supported at compile time.
				return new ExecuteCommand(this, LogicalValue.RUNTIME);
			}

			throw new IllegalStateException("Unhandled action: " + action);
		}

		return new ExecuteCommand(this, LogicalValue.TRUE);
	}

	@Override
	public Action initialLogicalValue(LocalScope scope) {
		return initialValue(scope).toInitialLogicalValue();
	}

	@Override
	public ImperativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		if (!isTopLevel()) {

			final ImperativeBlock reproduction =
				reproducer.getStatements().braces(this, getName());

			reproduceSentences(reproducer, reproduction);

			return null;
		}

		final ImperativeBlock reproduction = new ImperativeBlock(
				this,
				reproducer.distribute(),
				reproducer.getMemberRegistry(),
				getSentenceFactory());

		reproduceSentences(reproducer, reproduction);

		return reproduction;
	}

	public Statement wrap(Distributor distributor) {
		if (!isTopLevel()) {
			return this;
		}
		return new BracesWithinDeclaratives(this, distributor, this);
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return new ImperativeBlockOp(builder, this);
	}

	@Override
	Trace getTrace() {
		return this.trace;
	}

	public static final class BlockDistributor extends Distributor {

		private final LocalScope scope;

		public BlockDistributor(LocalScope scope) {
			this.scope = scope;
		}

		@Override
		public LocalScope getScope() {
			return this.scope;
		}

		@Override
		public Container getContainer() {
			return this.scope;
		}

		@Override
		public ScopePlace getPlace() {
			return localPlace(getScope(), FIRST_PLACE);
		}

	}

	private static final class ImperativeEnv extends StatementEnv {

		private final ImperativeBlock block;
		private final StatementEnv initialEnv;

		ImperativeEnv(ImperativeBlock block, StatementEnv initialEnv) {
			this.initialEnv = initialEnv;
			this.block = block;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.initialEnv.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.initialEnv.precondition(scope).and(
					localDef(this.block, scope).fullLogical());
		}

		@Override
		public String toString() {
			return this.initialEnv + ", " + this.block;
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.initialEnv.getExpectedType();
		}

	}

}
