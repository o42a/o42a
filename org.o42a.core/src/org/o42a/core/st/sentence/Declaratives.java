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

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.imperative.BracesWithinDeclaratives;
import org.o42a.core.value.ValueType;


public class Declaratives extends Statements<Declaratives> {

	private DeclarativesEnv env;
	private StatementEnv prevEnv;
	private StatementEnv lastDefinitionEnv;
	private DefinitionTargets definitionTargets;
	private final ArrayList<StatementEnv> statementEnvs =
		new ArrayList<StatementEnv>(1);

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
				for (int j = i + 1; j < size; ++j) {
					redundantDefinitions(
							targets.lastDeclaration(),
							statements.get(j));
				}
				result = targets;
				continue;
			}
			result = targets.add(result);
		}

		return this.definitionTargets = result;
	}

	public final StatementEnv getEnv() {
		if (this.env != null) {
			return this.env;
		}
		return this.env = new DeclarativesEnv();
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
		this.prevEnv = statement.setEnv(
				this.prevEnv != null
				? this.prevEnv : getSentence().getAltEnv());
		this.statementEnvs.add(this.prevEnv);
	}

	@Override
	protected void removeStatement(int index) {
		super.removeStatement(index);
		this.statementEnvs.remove(index);
	}

	protected Definitions define(Scope scope) {

		final DefinitionTargets kinds = getDefinitionTargets();

		if (!kinds.haveDefinition()) {
			return null;
		}

		final List<Statement> statements = getStatements();

		for (int i = statements.size() - 1; i >= 0; --i) {

			final Statement statement = statements.get(i);
			final Definitions definitions = statement.define(scope);

			if (definitions != null) {
				return definitions;
			}
		}

		throw new IllegalStateException("Missing definition: "+ this);
	}

	private StatementEnv lastDefinitionEnv() {
		if (this.lastDefinitionEnv != null) {
			return this.lastDefinitionEnv;
		}

		final List<Statement> statements = getStatements();

		for (int i = statements.size() - 1; i >= 0; --i) {

			final Statement statement = statements.get(i);

			if (!statement.getDefinitionTargets().haveDefinition()) {
				continue;
			}

			return this.lastDefinitionEnv = this.statementEnvs.get(i);
		}

		return this.lastDefinitionEnv = getSentence().getAltEnv();
	}

	private void redundantDefinitions(
			DefinitionTarget declaration,
			Statement statement) {

		final DefinitionTargets targets = statement.getDefinitionTargets();

		if (targets.haveCondition()) {
			if (declaration.isValue()) {
				getLogger().error(
						"redundant_condition_after_value",
						targets.firstCondition().getLoggable()
						.setPreviousLoggable(declaration.getLoggable()),
						"Condition is redunant, as it follows the "
						+ " value assignment statement");
			} else {
				getLogger().error(
						"redundant_condition_after_field",
						targets.firstCondition().getLoggable()
						.setPreviousLoggable(declaration.getLoggable()),
						"Condition is redunant, as it follows the "
						+ " field declaration");
			}
		}

		final DefinitionTarget ambiguity =
			targets.first(declaration.getDefinitionKey());

		if (ambiguity != null) {
			if (declaration.isValue()) {
				getLogger().error(
						"ambiguous_value",
						ambiguity.getLoggable().setPreviousLoggable(
								declaration.getLoggable()),
						"Ambiguous value declaration");
			} else {
				getLogger().error(
						"ambiguous_field",
						ambiguity.getLoggable().setPreviousLoggable(
								declaration.getLoggable()),
						"Ambiguous declaration of field '%s'",
						declaration.getFieldKey().getMemberId());
			}
		}
	}

	private final class DeclarativesEnv extends StatementEnv {

		@Override
		public Logical prerequisite(Scope scope) {
			return lastDefinitionEnv().prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return lastDefinitionEnv().precondition(scope);
		}

		@Override
		public String toString() {
			if (Declaratives.this.prevEnv != null) {
				return Declaratives.this.prevEnv.toString();
			}
			return Declaratives.this + "?";
		}

		@Override
		protected ValueType<?> expectedType() {
			return getSentence().getBlock()
			.getInitialEnv().getExpectedType();
		}

	}

}
