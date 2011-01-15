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

import static org.o42a.core.def.Definitions.postConditionDefinitions;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Cond;
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
	public void statement(St statement) {
		if (statement == null) {
			return;
		}
		super.statement(statement);
		this.lastConditions = statement.setConditions(lastConditions());
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(new BracesWithinDeclaratives(
				braces,
				nextDistributor(),
				braces));
	}

	protected Cond condition(Scope scope) {
		if (!getKind().hasCondition()) {
			return null;
		}

		Cond condition = null;

		for (St statement : getStatements()) {
			condition = Cond.and(condition, statement.condition(scope));
		}

		return condition;
	}

	protected Definitions define(DefinitionTarget target) {
		if (!getKind().hasCondition()) {
			return null;
		}
		if (!getKind().hasDefinition()) {
			if (target.isField()) {
				return null;
			}

			final Cond condition = condition(target.getScope());

			return postConditionDefinitions(
					condition,
					target.getScope(),
					condition);
		}

		final List<St> statements = getStatements();
		final int size = statements.size();

		Definitions result = null;
		int definitionIdx = 0;

		for (int i = 0; i < size; ++i) {

			final St statement = statements.get(i);
			final Definitions definition = statement.define(target);

			if (definition == null) {
				continue;
			}
			if (result == null) {
				definitionIdx = i;
				result = definition;
				continue;
			}

			final MemberKey memberKey = target.getMemberKey();

			if (memberKey != null) {
				getLogger().ambiguousField(definition, memberKey.toString());
			} else {
				getLogger().ambiguousValue(definition);
			}
		}

		final Cond condition =
			statementsCondition(target.getScope(), definitionIdx);

		if (result != null) {
			// have result - return it
			if (condition == null) {
				return result;
			}
			return result.and(condition);
		}
		if (condition == null) {
			return null;
		}
		if (target.isField()) {
			// no field declaration present
			return null;
		}

		return postConditionDefinitions(
				Declaratives.this,
				target.getScope(),
				condition);
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

	private Cond statementsCondition(Scope scope, int length) {

		final List<St> statements = getStatements();
		Cond condition = null;

		for (int i = 0; i < length; ++i) {
			condition = Cond.and(condition, statements.get(i).condition(scope));
		}

		return condition;
	}

	private final class DeclarativeConditions extends Conditions {

		@Override
		public Cond getPrerequisite() {
			return lastConditions().getPrerequisite();
		}

		@Override
		public Cond getCondition() {
			return lastConditions().getCondition();
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
