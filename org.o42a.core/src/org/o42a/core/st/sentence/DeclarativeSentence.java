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
import static org.o42a.core.ref.Logical.disjunction;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import java.util.List;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueType;


public abstract class DeclarativeSentence extends Sentence<Declaratives> {

	private InitialConditions initialConditions;
	private SentenceConditions conditions;
	private DefinitionTargets definitionTargets;
	private boolean ignored;

	DeclarativeSentence(
			LocationInfo location,
			DeclarativeBlock block,
			DeclarativeFactory sentenceFactory) {
		super(location, block, sentenceFactory);
	}

	@Override
	public DeclarativeBlock getBlock() {
		return (DeclarativeBlock) super.getBlock();
	}

	@Override
	public DeclarativeFactory getSentenceFactory() {
		return (DeclarativeFactory) super.getSentenceFactory();
	}

	@Override
	public DeclarativeSentence getPrerequisite() {
		return (DeclarativeSentence) super.getPrerequisite();
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		if (this.definitionTargets != null) {
			return this.definitionTargets;
		}

		DefinitionTargets result = noDefinitions();

		for (Declaratives alt : getAlternatives()) {

			final DefinitionTargets targets = alt.getDefinitionTargets();

			if (targets.isEmpty()) {
				continue;
			}
			if (result.isEmpty()) {
				result = targets;
				continue;
			}
			if (targets.haveDeclaration()) {
				if (result.haveDeclaration()) {
					reportAmbiguity(result, targets);
					result = result.add(targets);
					continue;
				}

				final DefinitionTarget declaration = targets.firstDeclaration();

				if (declaration.isValue()) {
					getLogger().error(
							"unexpected_value_alt",
							declaration.getLoggable().setPreviousLoggable(
									result.lastCondition().getLoggable()),
							"Alternative should not contain value assignment, "
							+ " because previous one contains only condition");
					continue;
				}

				getLogger().error(
						"unexpected_field_alt",
						declaration.getLoggable().setPreviousLoggable(
								result.lastCondition().getLoggable()),
						"Alternative should not contain field declaration, "
						+ " because previous one contains only condition");
				continue;
			}
			if (!result.haveDeclaration()) {
				result = result.add(targets);
				continue;
			}

			final DefinitionTarget declaration = result.lastDeclaration();

			if (declaration.isValue()) {
				getLogger().error(
						"unexpected_condition_alt_after_value",
						targets.firstCondition().getLoggable()
						.setPreviousLoggable(declaration.getLoggable()),
						"Alternative should contain condition, "
						+ " because previous one contains value assignment");
			}
			getLogger().error(
					"unexpected_condition_alt_after_field",
					targets.firstCondition().getLoggable()
					.setPreviousLoggable(declaration.getLoggable()),
					"Alternative should contain condition, "
					+ " because previous one contains field declaration");
		}

		return this.definitionTargets = result;
	}

	protected Definitions define(Scope scope) {

		final DefinitionTargets targets = getDefinitionTargets();

		if (!targets.haveDefinition()) {
			return null;
		}
		if (!targets.haveValue()) {

			final Logical fullLogical =
				getConditions().fullLogical(scope);

			return conditionDefinitions(fullLogical, scope, fullLogical);
		}

		for (Declaratives alt : getAlternatives()) {

			final Definitions definitions = alt.define(scope);

			if (definitions != null) {
				return definitions;
			}
		}

		throw new IllegalStateException("Value expected");
	}

	Conditions getInitialConditions() {
		if (this.initialConditions != null) {
			return this.initialConditions;
		}
		return this.initialConditions = new InitialConditions(this);
	}

	Conditions getConditions() {
		if (this.conditions != null) {
			return this.conditions;
		}
		return this.conditions = new SentenceConditions(this);
	}

	final boolean isIgnored() {
		return this.ignored;
	}

	final void ignore() {
		this.ignored = true;
	}

	private void reportAmbiguity(
			DefinitionTargets result,
			DefinitionTargets targets) {
		for (DefinitionKey key : targets) {
			if (!key.isDeclaration()) {
				continue;
			}

			final DefinitionTarget previousDeclaration = result.last(key);

			if (previousDeclaration == null) {
				continue;
			}
			if (key.isValue()) {
				getLogger().error(
						"ambiguous_value",
						targets.first(key).getLoggable().setPreviousLoggable(
								previousDeclaration.getLoggable()),
						"Ambiguous value");
				continue;
			}
			getLogger().error(
					"ambiguous_field",
					targets.first(key).getLoggable().setPreviousLoggable(
							previousDeclaration.getLoggable()),
					"Ambiguous declaration of field '%s'",
					previousDeclaration.getFieldKey().getMemberId());
		}
	}

	static final class Proposition extends DeclarativeSentence {

		Proposition(
				LocationInfo location,
				DeclarativeBlock block,
				DeclarativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

		@Override
		public boolean isClaim() {
			return false;
		}

		@Override
		public boolean isIssue() {
			return false;
		}

	}

	static final class Claim extends DeclarativeSentence {

		Claim(
				LocationInfo location,
				DeclarativeBlock block,
				DeclarativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

		@Override
		public boolean isClaim() {
			return true;
		}

		@Override
		public boolean isIssue() {
			return false;
		}

		@Override
		protected Definitions define(Scope scope) {

			final Definitions definitions = super.define(scope);

			if (definitions == null) {
				return null;
			}

			return definitions.claim();
		}

	}

	private static final class InitialConditions extends Conditions {

		private final DeclarativeSentence sentence;

		InitialConditions(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public Logical prerequisite(Scope scope) {

			final Conditions initial =
				this.sentence.getBlock().getInitialConditions();
			final DeclarativeSentence prerequisite =
				this.sentence.getPrerequisite();

			if (prerequisite == null) {
				return initial.prerequisite(scope);
			}

			return initial.prerequisite(scope).and(
					prerequisite.getConditions().fullLogical(scope));
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.sentence.getBlock()
			.getInitialConditions().precondition(scope);
		}

		@Override
		public String toString() {

			final DeclarativeSentence prerequisite =
				this.sentence.getPrerequisite();

			if (prerequisite != null) {
				return prerequisite + "? " + this.sentence;
			}

			return this.sentence.toString();
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.sentence.getBlock()
			.getInitialConditions().getExpectedType();
		}

	}

	private static final class SentenceConditions extends Conditions {

		private final DeclarativeSentence sentence;

		SentenceConditions(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.sentence.getInitialConditions().prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {

			final List<Declaratives> alternatives =
				this.sentence.getAlternatives();
			final int size = alternatives.size();

			if (size <= 1) {
				if (size == 0) {
					return this.sentence.getInitialConditions()
					.precondition(scope);
				}
				return alternatives.get(0).getConditions().fullLogical(scope);
			}

			final Logical[] vars = new Logical[size];

			for (int i = 0; i < size; ++i) {
				vars[i] =
					alternatives.get(i).getConditions().fullLogical(scope);
			}

			return disjunction(this.sentence, this.sentence.getScope(), vars);
		}

		@Override
		public String toString() {
			return "(" + this.sentence + ")?";
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.sentence.getBlock()
			.getInitialConditions().getExpectedType();
		}

	}

}
