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
import org.o42a.core.st.*;
import org.o42a.core.value.ValueType;
import org.o42a.util.Place;
import org.o42a.util.Place.Trace;


public abstract class Statements<S extends Statements<S>> extends Placed {

	private final Sentence<S> sentence;
	private final boolean opposite;
	private final ArrayList<St> statements = new ArrayList<St>();
	private boolean instructionsExecuted;
	private StatementKind kind;
	private ValueType<?> valueType;

	Statements(
			LocationSpec location,
			Sentence<S> sentence,
			boolean opposite) {
		super(location, new StatementsDistributor(sentence));
		this.sentence = sentence;
		this.opposite = opposite;
	}

	public Sentence<S> getSentence() {
		return this.sentence;
	}

	public final boolean isOpposite() {
		return this.opposite;
	}

	public final List<St> getStatements() {
		return this.statements;
	}

	public SentenceFactory<S, ?, ?> getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public final MemberRegistry getMemberRegistry() {
		return getSentence().getMemberRegistry();
	}

	public StatementKind getKind() {
		if (this.kind != null) {
			return this.kind;
		}

		executeInstructions();

		final List<St> statements = getStatements();
		St lastCondition = null;
		StatementKind result = StatementKind.EMPTY;

		for (int i = statements.size() - 1; i >= 0; --i) {

			final St statement = statements.get(i);
			final StatementKind kind = statement.getKind();

			if (!kind.hasDefinition()) {
				if (lastCondition != null) {
					continue;
				}
				if (kind.hasLogicalValue()) {
					lastCondition = statement;
				}
			} else {
				if (lastCondition != null) {
					getLogger().expectedDefinition(lastCondition);
					return this.kind = StatementKind.LOGICAL;
				}
				if (kind.hasValue()) {
					return this.kind = StatementKind.VALUE;
				}
			}

			result = kind;
		}

		return this.kind = result;
	}

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

	public void expression(Ref expression) {
		statement(new RefCondition(expression.rescope(getScope())));
	}

	public void assign(Ref value) {
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

	public Group group(LocationSpec location, ClauseDeclaration declaration) {
		assert declaration.getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + declaration;

		final ClauseBuilder builder =
			getMemberRegistry().newClause(declaration);

		if (builder == null) {
			return null;
		}

		return new Group(location, this, builder);
	}

	public Block<S> parentheses(LocationSpec location) {
		return parentheses(location, getContainer());
	}

	public Block<S> parentheses(LocationSpec location, Container container) {
		return parentheses(
				-1,
				location,
				container,
				getMemberRegistry());
	}

	public final ImperativeBlock braces(LocationSpec location) {
		return braces(location, null, getContainer());
	}

	public final ImperativeBlock braces(LocationSpec location, String name) {
		return braces(location, name, getContainer());
	}

	public final ImperativeBlock braces(
			LocationSpec location,
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

	public abstract void ellipsis(LocationSpec location, String name);

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

	public final void statement(St statement) {
		if (statement == null) {
			return;
		}
		statement(-1, statement);
	}

	protected abstract void braces(ImperativeBlock braces);

	protected void addStatement(St statement) {
		this.statements.add(statement);
	}

	final Trace getTrace() {
		return getSentence().getBlock().getTrace();
	}

	ValueType<?> valueType(ValueType<?> expected) {

		ValueType<?> result = expected;
		boolean hasResult = false;

		for (St statement : getStatements()) {
			if (!statement.getKind().hasValue()) {
				continue;
			}

			final ValueType<?> type = statement.getValueType();

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

		for (St statement : getStatements()) {

			final St statementReproduction = statement.reproduce(
					statementsReproducer.distributeBy(
							reproduction.nextDistributor()));

			if (statementReproduction != null) {
				reproduction.statement(statementReproduction);
			}
		}
	}

	private Block<S> parentheses(
			int index,
			LocationSpec location,
			Container container,
			MemberRegistry memberRegistry) {

		@SuppressWarnings("unchecked")
		final Block<S> parentheses =
			getSentence().getSentenceFactory().createParentheses(
					location,
					nextDistributor(container),
					(S) this);

		statement(index, parentheses);

		return parentheses;
	}

	private void statement(int index, St statement) {
		if (index < 0) {
			addStatement(statement);
		} else {
			this.statements.set(index, statement);
		}
	}

	void executeInstructions() {
		if (this.instructionsExecuted) {
			return;
		}
		this.instructionsExecuted = true;

		final List<St> statements = getStatements();

		for (int i = 0; i < statements.size(); ++i) {

			final St statement = statements.get(i);
			final Instruction instruction =
				statement.toInstruction(getScope(), true);

			if (instruction == null) {
				continue;
			}

			switch (instruction.getInstructionKind()) {
			case GENERIC_INSTRUCTION:
				instruction.execute();
				break;
			case REPLACEMENT_INSTRUCTION:

				final Block<S> block = parentheses(
					i,
					this,
					getContainer(),
					getMemberRegistry());

				instruction.execute(block);
				break;
			default:
				throw new IllegalStateException(
						"Unsupported instruction kind: "
						+ instruction.getInstructionKind());
			}

		}

		this.instructionsExecuted = true;
	}

	private static final class StatementsDistributor extends Distributor {

		private final Sentence<?> sentence;
		private final ScopePlace place;

		private StatementsDistributor(Sentence<?> sentence) {
			this.sentence = sentence;

			final Trace trace = this.sentence.getBlock().getTrace();

			if (trace == null) {
				this.place = scopePlace(getScope());
			} else {
				this.place = localPlace(getScope().toLocal(), trace.next());
			}
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

}
