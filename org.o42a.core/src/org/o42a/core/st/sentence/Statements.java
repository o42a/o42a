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
import static org.o42a.core.ScopePlace.scopePlace;
import static org.o42a.util.use.User.dummyUser;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.*;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.declarative.RefCondition;
import org.o42a.core.value.ValueType;
import org.o42a.util.Place;
import org.o42a.util.Place.Trace;
import org.o42a.util.log.Loggable;


public abstract class Statements<S extends Statements<S>> extends Placed {

	private final Sentence<S> sentence;
	private final boolean opposite;
	private final ArrayList<Statement> statements = new ArrayList<Statement>(1);
	private boolean instructionsExecuted;
	private ValueType<?> valueType;

	Statements(
			LocationInfo location,
			Sentence<S> sentence,
			boolean opposite) {
		super(location, new StatementsDistributor(location, sentence));
		this.sentence = sentence;
		this.opposite = opposite;
	}

	public Sentence<S> getSentence() {
		return this.sentence;
	}

	public final boolean isOpposite() {
		return this.opposite;
	}

	public final List<Statement> getStatements() {
		return this.statements;
	}

	public SentenceFactory<S, ?, ?> getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public final MemberRegistry getMemberRegistry() {
		return getSentence().getMemberRegistry();
	}

	public abstract DefinitionTargets getDefinitionTargets();

	public ValueType<?> getValueType() {
		if (this.valueType != null) {
			return this.valueType;
		}

		this.valueType = valueType(null);

		if (this.valueType != null) {
			return this.valueType;
		}

		return this.valueType = ValueType.VOID;
	}

	public final void expression(Ref expression) {
		assert expression.getContext() == getContext() :
			expression + " has wrong context: " + expression.getContext()
			+ ", but " + getContext() + " expected";
		statement(new RefCondition(expression.rescope(getScope())));
	}

	public final void assign(Ref value) {
		assert value.getContext() == getContext() :
			value + " has wrong context: " + value.getContext()
			+ ", but " + getContext() + " expected";
		statement(value.rescope(getScope()));
	}

	public FieldBuilder field(
			FieldDeclaration declaration,
			FieldDefinition definition) {

		final FieldBuilder builder =
			getMemberRegistry().newField(declaration, definition);

		if (builder == null) {
			return null;
		}

		return builder;
	}

	public final ClauseBuilder clause(ClauseDeclaration declaration) {
		assert declaration.getKind().isPlain() :
			"Plain clause declaration expected: " + declaration;
		return getMemberRegistry().newClause(declaration);
	}

	public Group group(LocationInfo location, ClauseDeclaration declaration) {
		assert declaration.getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + declaration;

		final ClauseBuilder builder =
			getMemberRegistry().newClause(declaration);

		if (builder == null) {
			return null;
		}

		return new Group(location, this, builder);
	}

	public Block<S> parentheses(LocationInfo location) {
		return parentheses(location, getContainer());
	}

	public Block<S> parentheses(LocationInfo location, Container container) {
		return parentheses(
				-1,
				location,
				nextDistributor(container),
				getMemberRegistry());
	}

	public final ImperativeBlock braces(LocationInfo location) {
		return braces(location, null, getContainer());
	}

	public final ImperativeBlock braces(LocationInfo location, String name) {
		return braces(location, name, getContainer());
	}

	public final ImperativeBlock braces(
			LocationInfo location,
			String name,
			Container container) {
		if (name != null) {

			final MemberRegistry memberRegistry = getMemberRegistry();

			if (!memberRegistry.declareBlock(location, name)) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		final S self = (S) this;
		final ImperativeBlock braces = getSentenceFactory().createBraces(
				location,
				nextDistributor(container),
				self,
				name);

		braces(braces);

		return braces;
	}

	public abstract void ellipsis(LocationInfo location, String name);

	public final Distributor nextDistributor() {

		final Trace trace = getTrace();

		if (trace == null) {
			return distribute();
		}

		return new NextDistributor(getContainer(), trace.next());
	}

	public final Distributor nextDistributor(Container container) {

		final Trace trace = getTrace();

		if (trace == null) {
			return distributeIn(container);
		}

		return new NextDistributor(container, trace.next());
	}

	public final void statement(Statement statement) {
		if (statement == null) {
			return;
		}
		addStatement(statement);
	}

	@Override
	public String toString() {

		final List<Statement> statements = getStatements();

		if (statements.isEmpty()) {
			return "<no statements>";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		for (Statement statement : statements) {
			if (!comma) {
				comma = true;
			} else {
				out.append(", ");
			}
			if (statement instanceof Ref) {
				out.append('=');
			}
			out.append(statement);
		}

		return out.toString();
	}

	protected abstract void braces(ImperativeBlock braces);

	protected void addStatement(Statement statement) {
		assert !this.instructionsExecuted :
			"Instructions already executed. Can not add statement " + statement;
		this.statements.add(statement);
	}

	protected void replaceStatement(int index, Statement statement) {
		this.statements.set(index, statement);
	}

	protected void removeStatement(int index) {
		this.statements.remove(index);
	}

	final Trace getTrace() {
		return getSentence().getBlock().getTrace();
	}

	ValueType<?> valueType(ValueType<?> expected) {

		ValueType<?> result = expected;
		boolean hasResult = false;

		for (Statement statement : getStatements()) {
			if (!statement.getDefinitionTargets().haveValue()) {
				continue;
			}

			final ValueType<?> type = statement.getValueType();

			if (type == null) {
				continue;
			}
			if (result == null) {
				result = type;
				hasResult = true;
				continue;
			}
			if (type == result) {
				hasResult = true;
				continue;
			}

			getLogger().incompatible(statement, result);
		}

		return hasResult ? result : null;
	}

	void reproduce(Sentence<S> sentence, Reproducer reproducer) {

		final S reproduction = sentence.alternative(this, isOpposite());
		final Reproducer statementsReproducer =
			reproducer.reproduceIn(reproduction);

		for (Statement statement : getStatements()) {

			final Statement statementReproduction = statement.reproduce(
					statementsReproducer.distributeBy(
							reproduction.nextDistributor()));

			if (statementReproduction != null) {
				reproduction.statement(statementReproduction);
			}
		}
	}

	final void resolveAll() {
		for (Statement statement : getStatements()) {
			statement.resolveAll();
		}
	}

	private Block<S> parentheses(
			int index,
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry) {

		@SuppressWarnings("unchecked")
		final Block<S> parentheses =
			getSentence().getSentenceFactory().createParentheses(
					location,
					distributor,
					(S) this);

		if (index < 0) {
			addStatement(parentheses);
		} else {
			replaceStatement(index, parentheses);
		}

		return parentheses;
	}

	void executeInstructions() {
		if (this.instructionsExecuted) {
			return;
		}
		this.instructionsExecuted = true;
		new InstructionCtx().executeAll();
	}

	private static final class StatementsDistributor extends Distributor {

		private final LocationInfo location;
		private final Sentence<?> sentence;
		private final ScopePlace place;

		StatementsDistributor(LocationInfo location, Sentence<?> sentence) {
			this.location = location;
			this.sentence = sentence;

			final Trace trace = this.sentence.getBlock().getTrace();

			if (trace == null) {
				this.place = scopePlace(getScope());
			} else {
				this.place = localPlace(getScope().toLocal(), trace.next());
			}
		}

		@Override
		public Loggable getLoggable() {
			return this.location.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.location.getContext();
		}

		@Override
		public Scope getScope() {
			return this.sentence.getScope();
		}

		@Override
		public Container getContainer() {
			return this.sentence.getContainer();
		}

		@Override
		public ScopePlace getPlace() {
			return this.place;
		}

	}

	private final class NextDistributor extends Distributor {

		private final Container container;
		private final LocalPlace place;

		NextDistributor(Container container, Place place) {
			this.container = container;
			this.place = localPlace(
					Statements.this.getScope().toLocal(),
					place);
		}

		@Override
		public CompilerContext getContext() {
			return Statements.this.getContext();
		}

		@Override
		public Loggable getLoggable() {
			return Statements.this.getLoggable();
		}

		@Override
		public ScopePlace getPlace() {
			return this.place;
		}

		@Override
		public Container getContainer() {
			return this.container;
		}

		@Override
		public Scope getScope() {
			return this.container.getScope();
		}

	}

	private final class InstructionCtx implements InstructionContext {

		private final Resolver resolver = getScope().newResolver(dummyUser());
		private int index;
		private Statement statement;
		private Block<?> block;
		private boolean doNotRemove;

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

			return this.block = parentheses(
					this.index,
					this.statement,
					this.statement.distribute(),
					getMemberRegistry());
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

			final List<Statement> statements = getStatements();

			while (this.index < statements.size()) {
				execute(statements.get(this.index));
			}
		}

		private final void execute(Statement statement) {

			final Instruction instruction =
				statement.toInstruction(getResolver(), true);

			if (instruction == null) {
				++this.index;
				return;
			}

			this.statement = statement;
			try {
				instruction.execute(this);
				if (!this.doNotRemove) {
					removeStatement(this.index);
				} else {
					++this.index;
				}
			} finally {
				this.block = null;
				this.doNotRemove = false;
			}
		}

	}

}
