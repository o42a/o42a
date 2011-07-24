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

import org.o42a.core.Scope;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.declarative.SentenceEnv;
import org.o42a.core.value.ValueType;


public abstract class DeclarativeSentence extends Sentence<Declaratives> {

	private AltEnv altEnv;
	private SentenceEnv env;
	private DefinitionTargets definitionTargets;
	private boolean ignored;

	protected DeclarativeSentence(
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

	public final StatementEnv getFinalEnv() {
		if (this.env != null) {
			return this.env;
		}
		return this.env = new SentenceEnv(this);
	}

	public final StatementEnv getAltEnv() {
		if (this.altEnv != null) {
			return this.altEnv;
		}
		return this.altEnv = new AltEnv(this);
	}

	public final boolean isIgnored() {
		return this.ignored;
	}

	public final void ignore() {
		this.ignored = true;
	}

	public Definitions define(Scope scope) {

		final DefinitionTargets targets = getDefinitionTargets();

		if (!targets.haveDefinition()) {
			return null;
		}
		if (!targets.haveValue()) {

			final Logical fullLogical = getFinalEnv().fullLogical(scope);
			final CondDef def = fullLogical.toCondDef();
			final DeclarativeSentence prerequisite = getPrerequisite();

			if (prerequisite == null) {
				return def.toDefinitions();
			}

			final CondDef withPrereq = def.addPrerequisite(
					prerequisite.getFinalEnv().fullLogical(getScope()));

			return withPrereq.toDefinitions();
		}

		for (Declaratives alt : getAlternatives()) {

			final Definitions definitions = alt.define(scope);

			if (definitions != null) {
				return definitions;
			}
		}

		throw new IllegalStateException("Value expected");
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

	private static final class AltEnv extends StatementEnv {

		private final DeclarativeSentence sentence;

		AltEnv(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public boolean hasPrerequisite() {
			if (this.sentence.getPrerequisite() != null) {
				return true;
			}

			final StatementEnv initial =
					this.sentence.getBlock().getInitialEnv();

			return initial.hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {

			final DeclarativeSentence prerequisite =
					this.sentence.getPrerequisite();

			if (prerequisite != null) {
				return prerequisite.getFinalEnv().fullLogical(scope);
			}

			final StatementEnv initial =
					this.sentence.getBlock().getInitialEnv();

			return initial.prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {

			final StatementEnv initial =
					this.sentence.getBlock().getInitialEnv();

			return initial.hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.sentence.getBlock().getInitialEnv().precondition(scope);
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
			return this.sentence.getBlock().getInitialEnv().getExpectedType();
		}

	}

}
