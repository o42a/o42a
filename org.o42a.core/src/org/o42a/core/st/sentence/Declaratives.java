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

import static org.o42a.core.def.Definitions.conditionDefinitions;
import static org.o42a.core.st.DefinitionTarget.noDefinitions;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Conditions;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.Statement;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.LogInfo;


public class Declaratives extends Statements<Declaratives> {

	private DeclarativeConditions conditions;
	private Conditions prevConditions;
	private Conditions lastDefinitionConditions;
	private DefinitionTargets definitionTargets;
	private final ArrayList<Conditions> statementConditions =
		new ArrayList<Conditions>(1);

	Declaratives(
			LocationInfo location,
			DeclarativeSentence sentence,
			boolean opposite) {
		super(location, sentence, opposite);
	}

	@Override
	public final DeclarativeSentence getSentence() {
		return (DeclarativeSentence) super.getSentence();
	}

	@Override
	public final DeclarativeFactory getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		if (this.definitionTargets != null) {
			return this.definitionTargets;
		}

		executeInstructions();

		final List<Statement> statements = getStatements();
		DefinitionTargets result = noDefinitions();
		final int size = statements.size();

		for (int i = size - 1; i >= 0; --i) {

			final Statement statement = statements.get(i);
			final DefinitionTargets targets = statement.getDefinitionTargets();

			if (targets.isEmpty()) {
				continue;
			}
			if (targets.haveDeclaration()) {
				if (result.haveCondition()) {
					for (int j = i + 1; j < size; ++j) {
						redundantConditions(statement, statements.get(j));
					}
				}
				result = targets;
				continue;
			}
			result = result.add(targets);
		}

		return this.definitionTargets = result;
	}

	@Override
	public final DeclarativeBlock parentheses(LocationInfo location) {
		return parentheses(location, getContainer());
	}

	@Override
	public final DeclarativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return (DeclarativeBlock) super.parentheses(location, container);
	}

	@Override
	public void ellipsis(LocationInfo location, String name) {
		getLogger().prohibitedDeclarativeEllipsis(location);
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(new BracesWithinDeclaratives(
				braces,
				nextDistributor(),
				braces));
	}

	@Override
	protected void addStatement(Statement statement) {
		super.addStatement(statement);
		this.prevConditions = statement.setConditions(
				this.prevConditions != null
				? this.prevConditions : getSentence().getInitialConditions());
		this.statementConditions.add(this.prevConditions);
	}

	protected Definitions define(Scope scope) {

		final DefinitionTargets kinds = getDefinitionTargets();

		if (!kinds.haveDefinition()) {
			return null;
		}
		if (!kinds.haveValue()) {

			final Logical condition =
				lastDefinitionConditions().fullLogical(scope);

			return conditionDefinitions(condition, scope, condition);
		}

		final List<Statement> statements = getStatements();
		final int size = statements.size();

		Definitions result = null;

		for (int i = 0; i < size; ++i) {

			final Statement statement = statements.get(i);
			final Definitions definition = statement.define(scope);

			if (definition == null) {
				continue;
			}
			if (result == null) {
				result = definition;
				continue;
			}
			getLogger().ambiguousValue(definition);
		}

		assert result != null :
			"Result is missing";

		return result;
	}

	Conditions getConditions() {
		if (this.conditions != null) {
			return this.conditions;
		}
		return this.conditions = new DeclarativeConditions();
	}

	private Conditions lastDefinitionConditions() {
		if (this.lastDefinitionConditions != null) {
			return this.lastDefinitionConditions;
		}

		final List<Statement> statements = getStatements();

		for (int i = statements.size() - 1; i >= 0; --i) {

			final Statement statement = statements.get(i);

			if (!statement.getDefinitionTargets().haveDefinition()) {
				continue;
			}

			return this.lastDefinitionConditions =
				this.statementConditions.get(i);
		}

		return this.lastDefinitionConditions =
			getSentence().getInitialConditions();
	}

	private void redundantConditions(Statement declaration, Statement statement) {

		final DeclarativeBlock block = statement.toDeclarativeBlock();

		if (block != null) {
			redundantConditions(declaration, block);
			return;
		}

		final DefinitionTargets statementKinds = statement.getDefinitionTargets();

		if (!statementKinds.haveCondition()) {
			return;
		}

		logRedundantCondition(declaration, statement, statementKinds);
	}

	private void redundantConditions(Statement declaration, DeclarativeBlock block) {

		final DefinitionTargets statementKinds = block.getDefinitionTargets();

		if (!statementKinds.haveCondition()) {
			return;
		}
		for (DeclarativeSentence sentence : block.getSentences()) {

			final DefinitionTargets sentenceStatementKinds =
				sentence.getDefinitionTargets();

			if (!sentenceStatementKinds.haveCondition()) {
				continue;
			}
			if (sentenceStatementKinds.onlyConditions()) {
				logRedundantCondition(
						declaration,
						sentence,
						sentenceStatementKinds);
				continue;
			}
			redundantConditions(declaration, sentence);
		}
	}

	private void redundantConditions(
			Statement declaration,
			DeclarativeSentence sentence) {
		for (Declaratives alt : sentence.getAlternatives()) {

			final DefinitionTargets altStatementKinds =
				alt.getDefinitionTargets();

			if (!altStatementKinds.haveCondition()) {
				continue;
			}
			if (altStatementKinds.onlyConditions()) {
				logRedundantCondition(
						declaration,
						alt,
						altStatementKinds);
				continue;
			}
			redundantConditions(declaration, alt);
		}
	}

	private void redundantConditions(Statement declaration, Declaratives alt) {
		for (Statement statement : alt.getStatements()) {
			redundantConditions(declaration, statement);
		}
	}

	private void logRedundantCondition(
			Statement declaration,
			LogInfo statement,
			DefinitionTargets statementKinds) {
		getLogger().error(
				"redundant_condition",
				statement.getLoggable().setPreviousLoggable(
						declaration.getLoggable()),
				"Condition is redunant, as it follows the field declaration "
				+ " or self-assignment statement");
	}

	private final class DeclarativeConditions extends Conditions {

		@Override
		public Logical prerequisite(Scope scope) {
			return lastDefinitionConditions().prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return lastDefinitionConditions().precondition(scope);
		}

		@Override
		public String toString() {
			if (Declaratives.this.prevConditions != null) {
				return Declaratives.this.prevConditions.toString();
			}
			return Declaratives.this + "?";
		}

		@Override
		protected ValueType<?> expectedType() {
			return getSentence().getBlock()
			.getInitialConditions().getExpectedType();
		}

	}

}
