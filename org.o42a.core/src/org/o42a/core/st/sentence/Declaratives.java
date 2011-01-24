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

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Conditions;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.St;


public class Declaratives extends Statements<Declaratives> {

	private DeclarativeConditions conditions;
	private Conditions lastConditions;

	Declaratives(
			LocationSpec location,
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
	public final DeclarativeBlock parentheses(LocationSpec location) {
		return parentheses(location, getContainer());
	}

	@Override
	public final DeclarativeBlock parentheses(
			LocationSpec location,
			Container container) {
		return (DeclarativeBlock) super.parentheses(location, container);
	}

	@Override
	public void ellipsis(LocationSpec location, String name) {
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
		if (!getKind().hasLogicalValue()) {
			return null;
		}
		if (!getKind().hasValue()) {
			if (getKind().hasDefinition()) {
				return null;
			}

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
