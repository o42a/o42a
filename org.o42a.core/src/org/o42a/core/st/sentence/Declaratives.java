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

import static org.o42a.core.source.CompilerLogger.logAnother;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.declarative.ExplicitInclusion;
import org.o42a.core.st.impl.imperative.BracesWithinDeclaratives;
import org.o42a.core.value.ValueStruct;


public class Declaratives extends Statements<Declaratives> {

	private DeclarativesEnv env;
	private Definer prevDefiner;
	private StatementEnv lastDefinitionEnv;
	private DefinitionTargets definitionTargets;

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

		final List<Definer> definers = getDefiners();
		DefinitionTargets result = noDefinitions();
		final int size = definers.size();

		for (int i = size - 1; i >= 0; --i) {

			final Definer definer = definers.get(i);
			final DefinitionTargets targets = definer.getDefinitionTargets();

			if (targets.isEmpty()) {
				continue;
			}
			if (targets.haveDeclaration()) {
				for (int j = i + 1; j < size; ++j) {
					redundantDefinitions(
							targets.lastDeclaration(),
							definers.get(j));
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
	public void assign(LocationInfo location, Ref destination, Ref value) {
		getLogger().error(
				"prohibited_declarative_assignment",
				location,
				"Location is not allowed within declarative block");
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
	public void include(LocationInfo location, String tag) {
		if (!getMemberRegistry().inclusions().include(location, tag)) {
			return;
		}
		statement(new ExplicitInclusion(location, this, tag));
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(new BracesWithinDeclaratives(
				braces,
				nextDistributor(),
				braces));
	}

	@Override
	protected Definer define(Statement statement) {

		final StatementEnv env;
		if (this.prevDefiner != null) {
			env = this.prevDefiner.nextEnv();
		} else {
			env = getSentence().getAltEnv();
		}

		return this.prevDefiner = statement.define(env);
	}

	protected Definitions define(Scope scope) {

		final DefinitionTargets kinds = getDefinitionTargets();

		if (!kinds.haveDefinition()) {
			return null;
		}

		final List<Definer> definers = getDefiners();

		for (int i = definers.size() - 1; i >= 0; --i) {

			final Definer definer = definers.get(i);
			final Definitions definitions = definer.define(scope);

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

		final List<Definer> definers = getDefiners();
		final int last = definers.size() - 1;

		for (int i = last; i >= 0; --i) {

			final Definer definer = definers.get(i);

			if (!definer.getDefinitionTargets().haveDefinition()) {
				continue;
			}

			if (i == last) {
				return this.lastDefinitionEnv = definer.nextEnv();
			}

			return this.lastDefinitionEnv = definers.get(i + 1).env();
		}

		return this.lastDefinitionEnv = getSentence().getAltEnv();
	}

	private void redundantDefinitions(
			DefinitionTarget declaration,
			Definer definer) {

		final DefinitionTargets targets = definer.getDefinitionTargets();

		if (targets.haveCondition()) {
			if (declaration.isValue()) {
				getLogger().error(
						"redundant_condition_after_value",
						targets.firstCondition().getLoggable().setReason(
								logAnother(declaration)),
						"Condition is redunant, as it follows the "
						+ " value assignment statement");
			} else {
				getLogger().error(
						"redundant_condition_after_field",
						targets.firstCondition().getLoggable().setReason(
								logAnother(declaration)),
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
						ambiguity.getLoggable().setReason(
								logAnother(declaration)),
						"Ambiguous value declaration");
			} else {
				getLogger().error(
						"ambiguous_field",
						ambiguity.getLoggable().setReason(
								logAnother(declaration)),
						"Ambiguous declaration of field '%s'",
						declaration.getFieldKey().getMemberId());
			}
		}
	}

	private final class DeclarativesEnv extends StatementEnv {

		@Override
		public boolean hasPrerequisite() {
			return lastDefinitionEnv().hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return lastDefinitionEnv().prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return lastDefinitionEnv().hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			return lastDefinitionEnv().precondition(scope);
		}

		@Override
		public String toString() {
			if (Declaratives.this.prevDefiner != null) {
				return Declaratives.this.prevDefiner.env().toString();
			}
			return Declaratives.this + "?";
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return getSentence().getBlock()
					.getInitialEnv().getExpectedValueStruct();
		}

	}

}
