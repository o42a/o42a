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
package org.o42a.core.st.sentence;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.NextDistributor;
import org.o42a.core.st.impl.StatementsDistributor;
import org.o42a.core.st.impl.imperative.Locals;
import org.o42a.util.Place.Trace;


public abstract class Statements<S extends Statements<S>> extends Placed {

	private final Sentence<S> sentence;
	private final boolean opposite;
	private final ArrayList<Definer> definers = new ArrayList<Definer>(1);
	private boolean instructionsExecuted;

	Statements(
			LocationInfo location,
			Sentence<S> sentence,
			boolean opposite) {
		super(
				location,
				new StatementsDistributor(
						location,
						sentence,
						sentence.getBlock().getTrace()));
		this.sentence = sentence;
		this.opposite = opposite;
	}

	public Sentence<S> getSentence() {
		return this.sentence;
	}

	public final boolean isOpposite() {
		return this.opposite;
	}

	public final List<Definer> getDefiners() {
		return this.definers;
	}

	public SentenceFactory<S, ?, ?> getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public final MemberRegistry getMemberRegistry() {
		return getSentence().getMemberRegistry();
	}

	public abstract DefinitionTargets getDefinitionTargets();

	public final void expression(Ref expression) {
		assert expression.getContext() == getContext() :
			expression + " has wrong context: " + expression.getContext()
			+ ", but " + getContext() + " expected";
		statement(expression.rescope(getScope()).toCondition());
	}

	public final void selfAssign(Ref value) {
		selfAssign(value, value);
	}

	public final void selfAssign(LocationInfo location, Ref value) {
		assert value.getContext() == getContext() :
			value + " has wrong context: " + value.getContext()
			+ ", but " + getContext() + " expected";
		if (getSentence().isIssue()) {
			getLogger().error(
					"porhibited_issue_assignment",
					location,
					"Assignment is prohibited within issue");
		}
		statement(value.rescope(getScope()));
	}

	public abstract void assign(
			LocationInfo location,
			Ref destination,
			Ref value);

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

			final Locals locals = getSentence().getBlock().getLocals();

			if (!locals.declareBlock(location, name)) {
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

	public abstract void include(LocationInfo location, String name);

	public final Distributor nextDistributor() {

		final Trace trace = getTrace();

		if (trace == null) {
			return distribute();
		}

		return new NextDistributor(this, getContainer(), trace.next());
	}

	public final Distributor nextDistributor(Container container) {

		final Trace trace = getTrace();

		if (trace == null) {
			return distributeIn(container);
		}

		return new NextDistributor(this, container, trace.next());
	}

	public final void statement(Statement statement) {
		if (statement == null) {
			return;
		}
		addStatement(statement);
	}

	@Override
	public String toString() {

		final List<Definer> definers = getDefiners();

		if (definers.isEmpty()) {
			return "<no statements>";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		for (Definer definer : definers) {
			if (!comma) {
				comma = true;
			} else {
				out.append(", ");
			}
			out.append(definer);
		}

		return out.toString();
	}

	protected abstract void braces(ImperativeBlock braces);

	protected final void addStatement(Statement statement) {
		assert !this.instructionsExecuted :
			"Instructions already executed. Can not add statement " + statement;
		this.definers.add(define(statement));
	}

	protected final void replaceStatement(int index, Statement statement) {

		final Definer old = this.definers.get(index);

		this.definers.set(index, old.replaceWith(statement));
	}

	protected final void removeStatement(int index) {
		this.definers.remove(index);
	}

	protected abstract Definer define(Statement statement);

	final Trace getTrace() {
		return getSentence().getBlock().getTrace();
	}

	void reproduce(Sentence<S> sentence, Reproducer reproducer) {

		final S reproduction = sentence.alternative(this, isOpposite());
		final Reproducer statementsReproducer =
				reproducer.reproduceIn(reproduction);

		for (Definer definer : getDefiners()) {

			final Statement statementReproduction =
					definer.getStatement().reproduce(
							statementsReproducer.distributeBy(
									reproduction.nextDistributor()));

			if (statementReproduction != null) {
				reproduction.statement(statementReproduction);
			}
		}
	}

	final void resolveImperatives(LocalResolver resolver) {
		assert this.instructionsExecuted :
			"Instructions not executed yet";
		for (Definer definer : getDefiners()) {
			definer.getStatement().resolveImperative(resolver);
		}
	}

	Block<S> parentheses(
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
		new InstructionExecutor(this).executeAll();
	}

}
