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
import static org.o42a.core.st.StatementKinds.NO_STATEMENTS;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.*;
import org.o42a.util.log.LogInfo;


public class Declaratives extends Statements<Declaratives> {

	private DeclarativeConditions conditions;
	private Conditions lastConditions;
	private StatementKinds statementKinds;

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
	public StatementKinds getStatementKinds() {
		if (this.statementKinds != null) {
			return this.statementKinds;
		}

		executeInstructions();

		final List<St> statements = getStatements();
		StatementKinds result = NO_STATEMENTS;
		final int size = statements.size();

		for (int i = size - 1; i >= 0; --i) {

			final St statement = statements.get(i);
			final StatementKinds kinds = statement.getStatementKinds();

			if (kinds.isEmpty()) {
				continue;
			}
			if (kinds.haveDeclaration()) {
				if (result.haveCondition()) {
					for (int j = i + 1; j < size; ++j) {
						redundantConditions(statement, statements.get(j));
					}
				}
				result = kinds;
				continue;
			}
			result = result.add(kinds);
		}

		return this.statementKinds = result;
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
	protected void addStatement(St statement) {
		super.addStatement(statement);
		this.lastConditions = statement.setConditions(lastConditions());
	}

	protected Definitions define(DefinitionTarget target) {

		final StatementKinds kinds = getStatementKinds();

		if (!kinds.haveDefinition()) {
			return null;
		}
		if (!kinds.haveValue()) {

			final Logical condition =
				lastConditions().fullLogical(target.getScope());

			return conditionDefinitions(
					condition,
					target.getScope(),
					condition);
		}

		final List<St> statements = getStatements();
		final int size = statements.size();

		Definitions result = null;

		for (int i = 0; i < size; ++i) {

			final St statement = statements.get(i);
			final Definitions definition = statement.define(target);

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

	private Conditions lastConditions() {
		if (this.lastConditions != null) {
			return this.lastConditions;
		}
		return this.lastConditions = getSentence().getInitialConditions();
	}

	private void redundantConditions(St declaration, St statement) {

		final DeclarativeBlock block = statement.toDeclarativeBlock();

		if (block != null) {
			redundantConditions(declaration, block);
			return;
		}

		final StatementKinds statementKinds = statement.getStatementKinds();

		if (!statementKinds.haveCondition()) {
			return;
		}

		logRedundantCondition(declaration, statement, statementKinds);
	}

	private void redundantConditions(St declaration, DeclarativeBlock block) {

		final StatementKinds statementKinds = block.getStatementKinds();

		if (!statementKinds.haveCondition()) {
			return;
		}
		for (DeclarativeSentence sentence : block.getSentences()) {

			final StatementKinds sentenceStatementKinds =
				sentence.getStatementKinds();

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
			St declaration,
			DeclarativeSentence sentence) {
		for (Declaratives alt : sentence.getAlternatives()) {

			final StatementKinds altStatementKinds =
				alt.getStatementKinds();

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

	private void redundantConditions(St declaration, Declaratives alt) {
		for (St statement : alt.getStatements()) {
			redundantConditions(declaration, statement);
		}
	}

	private void logRedundantCondition(
			St declaration,
			LogInfo statement,
			StatementKinds statementKinds) {
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
			return lastConditions().prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return lastConditions().precondition(scope);
		}

		@Override
		public String toString() {
			if (Declaratives.this.lastConditions != null) {
				return Declaratives.this.lastConditions.toString();
			}
			return Declaratives.this + "?";
		}

	}

}
