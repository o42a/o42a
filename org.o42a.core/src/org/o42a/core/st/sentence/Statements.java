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
package org.o42a.core.st.sentence;

import static org.o42a.core.st.impl.SentenceErrors.prohibitedIssueAssignment;
import static org.o42a.core.st.impl.SentenceErrors.prohibitedIssueBraces;
import static org.o42a.core.st.impl.SentenceErrors.prohibitedIssueField;

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
import org.o42a.core.ref.impl.cond.RefCondition;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Implication;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.impl.StatementsDistributor;
import org.o42a.core.st.impl.imperative.NamedBlocks;
import org.o42a.core.st.impl.local.LocalInsides;
import org.o42a.core.value.TypeParameters;
import org.o42a.util.string.Name;


public abstract class Statements<
		S extends Statements<S, L>,
		L extends Implication<L>>
				extends Contained {

	private final Sentence<S, L> sentence;
	private final ArrayList<L> implications = new ArrayList<>(1);
	private boolean statementDropped;
	private boolean instructionsExecuted;
	private boolean incompatibilityReported;
	private Container nextContainer;

	Statements(LocationInfo location, Sentence<S, L> sentence) {
		super(
				location,
				new StatementsDistributor(location, sentence));
		this.sentence = sentence;
		this.nextContainer = getContainer();
	}

	public Sentence<S, L> getSentence() {
		return this.sentence;
	}

	public final boolean isInsideIssue() {
		return getSentence().isInsideIssue();
	}

	public final List<L> getImplications() {
		return this.implications;
	}

	public SentenceFactory<L, S, ?, ?> getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public final MemberRegistry getMemberRegistry() {
		return getSentence().getMemberRegistry();
	}

	public final void expression(Ref expression) {
		assert expression.getContext() == getContext() :
			expression + " has wrong context: " + expression.getContext()
			+ ", but " + getContext() + " expected";
		statement(expression.rescope(getScope()).toCondition(this));
	}

	public final void selfAssign(Ref value) {
		selfAssign(value, value);
	}

	public final void selfAssign(LocationInfo location, Ref value) {
		assert value.getContext() == getContext() :
			value + " has wrong context: " + value.getContext()
			+ ", but " + getContext() + " expected";
		if (isInsideIssue()) {
			prohibitedIssueAssignment(location);
			dropStatement();
			return;
		}
		statement(value.rescope(getScope()).toValue(location, this));
	}

	public FieldBuilder field(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (isInsideIssue()) {
			prohibitedIssueField(declaration);
			dropStatement();
			return null;
		}

		final FieldBuilder builder =
				getMemberRegistry().newField(declaration, definition);

		if (builder == null) {
			dropStatement();
			return null;
		}

		return builder;
	}

	public final ClauseBuilder clause(ClauseDeclaration declaration) {
		assert declaration.getKind().isPlain() :
			"Plain clause declaration expected: " + declaration;

		final ClauseBuilder clause =
				getMemberRegistry().newClause(this, declaration);

		if (clause == null) {
			dropStatement();
		}

		return clause;
	}

	public Group group(LocationInfo location, ClauseDeclaration declaration) {
		assert declaration.getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + declaration;

		final ClauseBuilder builder =
				getMemberRegistry().newClause(this, declaration);

		if (builder == null) {
			dropStatement();
			return null;
		}

		return new Group(location, this, builder);
	}

	public Block<S, L> parentheses(LocationInfo location) {
		return parentheses(location, nextContainer());
	}

	public Block<S, L> parentheses(LocationInfo location, Container container) {
		return parentheses(
				-1,
				location,
				nextDistributor(container));
	}

	public final ImperativeBlock braces(LocationInfo location) {
		return braces(location, null, nextContainer());
	}

	public final ImperativeBlock braces(LocationInfo location, Name name) {
		return braces(location, name, nextContainer());
	}

	public final ImperativeBlock braces(
			LocationInfo location,
			Name name,
			Container container) {
		if (isInsideIssue()) {
			prohibitedIssueBraces(location);
			dropStatement();
			return null;
		}
		if (name != null) {

			final NamedBlocks namedBlocks =
					getSentence().getBlock().getNamedBlocks();

			if (!namedBlocks.declareBlock(location, name)) {
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

		if (braces != null) {
			braces(braces);
		} else {
			dropStatement();
		}

		return braces;
	}

	public final Local local(LocationInfo location, Name name, Ref ref) {

		final Sentence<S, L> sentence = getSentence();
		final Block<S, L> block = sentence.getBlock();

		block.getLocals().declareLocal(location, name);

		final Local local = new Local(location, name, ref);

		this.nextContainer = new LocalInsides(local);
		statement(new RefCondition(local));

		return local;
	}

	public abstract void ellipsis(LocationInfo location, Name name);

	public abstract void include(LocationInfo location, Name name);

	public final Container nextContainer() {
		return this.nextContainer;
	}

	public final Distributor nextDistributor() {
		return nextDistributor(nextContainer());
	}

	public final void statement(Statement statement) {
		if (statement == null) {
			dropStatement();
			return;
		}
		addStatement(statement);
	}

	public final boolean assertInstructionsExecuted() {
		assert this.instructionsExecuted :
			"Instructions not executed yet";
		return true;
	}

	@Override
	public String toString() {

		final List<L> implications = getImplications();

		if (implications.isEmpty()) {
			return "<no statements>";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		for (L implication : implications) {
			if (!comma) {
				comma = true;
			} else {
				out.append(", ");
			}
			out.append(implication);
		}

		return out.toString();
	}

	protected final void dropStatement() {
		this.statementDropped = true;
		getSentence().dropStatement();
	}

	protected abstract void braces(ImperativeBlock braces);

	protected final void addStatement(Statement statement) {
		assert !this.instructionsExecuted :
			"Instructions already executed. Can not add statement " + statement;
		this.implications.add(implicate(statement));
	}

	protected final void replaceStatement(int index, Statement statement) {

		final L old = this.implications.get(index);

		this.implications.set(index, old.replaceWith(statement));
	}

	protected final void removeStatement(int index) {
		this.implications.remove(index);
	}

	protected abstract L implicate(Statement statement);

	void reproduce(Sentence<S, L> sentence, Reproducer reproducer) {

		final S reproduction = sentence.alternative(this);
		final Reproducer statementsReproducer =
				reproducer.reproduceIn(reproduction);

		for (L implication : getImplications()) {

			final Statement statementReproduction =
					implication.getStatement().reproduce(
							statementsReproducer.distributeBy(
									reproduction.nextDistributor()));

			if (statementReproduction != null) {
				reproduction.statement(statementReproduction);
			}
		}
	}

	Block<S, L> parentheses(
			int index,
			LocationInfo location,
			Distributor distributor) {

		final Block<S, L> parentheses =
				getSentence().getSentenceFactory().createParentheses(
						location,
						distributor,
						self());

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
		new InstructionExecutor(this).executeAll();
	}

	TypeParameters<?> typeParameters(
			Scope scope,
			TypeParameters<?> expectedParameters) {
		executeInstructions();

		// Statements contain at most one value.
		for (Implication<?> implication : getImplications()) {
			if (!implication.getStatement().isValid()) {
				continue;
			}

			final TypeParameters<?> typeParameters =
					implication.typeParameters(scope);

			if (typeParameters == null) {
				continue;
			}
			if (!expectedParameters.assignableFrom(typeParameters)) {
				if (!this.incompatibilityReported) {
					this.incompatibilityReported = true;
					scope.getLogger().incompatible(
							implication.getLocation(),
							expectedParameters);
				}
				return null;
			}

			return typeParameters;
		}

		return null;
	}

	void reportEmptyAlternative() {
		if (!this.statementDropped) {
			getLogger().error(
					"prohibited_empty_alternative",
					this,
					"Empty alternative");
		}
	}

	@SuppressWarnings("unchecked")
	private final S self() {
		return (S) this;
	}

	private final Distributor nextDistributor(Container container) {
		return distributeIn(container);
	}

}
