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

import static org.o42a.core.source.CompilerLogger.logAnotherLocation;
import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;
import static org.o42a.core.st.Definer.noDefs;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;
import static org.o42a.core.st.impl.SentenceErrors.declarationNotAlone;

import org.o42a.core.Scope;
import org.o42a.core.object.def.CondDef;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.declarative.SentenceEnv;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueStruct;


public abstract class DeclarativeSentence
		extends Sentence<Declaratives, Definer> {

	private AltEnv altEnv;
	private SentenceEnv env;
	private DefTargets targets;
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

	public final boolean isInsideClaim() {
		if (isClaim()) {
			return true;
		}
		return getBlock().isInsideClaim();
	}

	public DefTargets getDefTargets() {
		if (this.targets != null) {
			return this.targets;
		}
		return this.targets = addSentenceTargets(altTargets());
	}

	public DefinitionTargets getDefinitionTargets() {
		if (this.definitionTargets != null) {
			return this.definitionTargets;
		}

		final DefinitionTargets prerequisiteTargets;
		final DeclarativeSentence prerequisite = getPrerequisite();

		if (prerequisite == null) {
			prerequisiteTargets = noDefinitions();
		} else {
			prerequisiteTargets = prerequisite.getDefinitionTargets();
		}

		return this.definitionTargets =
				prerequisiteTargets.add(altDefinitionTargets());
	}

	public final DefinerEnv getFinalEnv() {
		if (this.env != null) {
			return this.env;
		}
		return this.env = new SentenceEnv(this);
	}

	public final DefinerEnv getAltEnv() {
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
				return def.toDefinitions(
						getBlock().getInitialEnv().getExpectedValueStruct());
			}

			final CondDef withPrereq = def.addPrerequisite(
					prerequisite.getFinalEnv().fullLogical(getScope()));

			return withPrereq.toDefinitions(
					getBlock().getInitialEnv().getExpectedValueStruct());
		}
		for (Declaratives alt : getAlternatives()) {

			final Definitions definitions = alt.define(scope);

			if (definitions != null) {
				return definitions;
			}
		}

		throw new IllegalStateException("Value expected");
	}

	public DefValue value(Resolver resolver) {

		final DeclarativeSentence prerequisite = getPrerequisite();

		if (prerequisite != null) {

			final DefValue prereqValue = prerequisite.value(resolver);

			assert !prereqValue.hasValue() :
				"Prerequisite can not have a value";

			final LogicalValue logicalValue = prereqValue.getLogicalValue();

			if (!logicalValue.isTrue()) {
				return logicalValue.negate().toDefValue();
			}
		}

		DefValue result = TRUE_DEF_VALUE;

		for (Declaratives alt : getAlternatives()) {

			final DefValue value = alt.value(resolver);

			if (value.hasValue()) {
				return value;
			}

			final LogicalValue logicalValue = value.getLogicalValue();

			if (!logicalValue.isTrue()) {
				if (logicalValue.isFalse()) {
					result = value;
					continue;
				}
				return value;
			}
		}

		return result;
	}

	private DefTargets altTargets() {

		DefTargets result = noDefs();
		Declaratives first = null;

		for (Declaratives alt : getAlternatives()) {

			final DefTargets targets = alt.getDefTargets();

			if (first == null) {
				first = alt;
			} else if (result.isEmpty()) {
				if (!result.haveError()) {
					first.reportEmptyAlternative();
				}
				result = result.addError();
				continue;
			} else if (!result.defining()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), result);
				}
				result = result.addError();
				continue;
			} else if (!targets.defining()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), targets);
				}
				result = result.addError();
				continue;
			}
			if (result.isEmpty()) {
				result = targets;
				continue;
			}
			result = result.add(targets);

			final boolean mayBeNonBreaking =
					(result.breaking() || targets.breaking())
					&& result.breaking() != targets.breaking();

			if (mayBeNonBreaking) {
				result = result.addPrerequisite();
			}
		}

		return result;
	}

	private DefTargets addSentenceTargets(DefTargets targets) {

		final DefTargets result;

		if (isIssue() && targets.isEmpty() && !targets.haveError()) {
			reportEmptyIssue();
			result = targets.addError();
		} else {
			result = targets;
		}
		if (!isInsideClaim()) {
			return result;
		}

		return result.claim();
	}

	private DefinitionTargets altDefinitionTargets() {

		DefinitionTargets result = noDefinitions();

		for (Declaratives alt : getAlternatives()) {

			final DefinitionTargets targets = alt.getDefinitionTargets();

			if (targets.isEmpty()) {
				result = result.add(targets);
				continue;
			}
			if (result.isEmpty()) {
				result = targets;
				continue;
			}
			if (targets.haveDeclaration()) {
				if (result.haveDeclaration()) {
					result = reportAmbiguity(result, targets);
					continue;
				}

				final DefinitionTarget declaration = targets.firstDeclaration();

				if (declaration.isValue()) {
					result = result.addError();
					getLogger().error(
							"unexpected_value_alt",
							logAnotherLocation(
									declaration,
									result.lastCondition()),
							"Alternative should not contain value assignment, "
							+ " because previous one contains only condition");
					continue;
				}

				getLogger().error(
						"unexpected_field_alt",
						logAnotherLocation(
								declaration,
								result.lastCondition()),
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
						logAnotherLocation(
								targets.firstCondition(),
								declaration),
						"Alternative should contain condition, "
						+ " because previous one contains value assignment");
				continue;
			}
			getLogger().error(
					"unexpected_condition_alt_after_field",
					logAnotherLocation(targets.firstCondition(), declaration),
					"Alternative should contain condition, "
					+ " because previous one contains field declaration");
		}

		if (isIssue() && result.isEmpty() && !result.haveError()) {
			reportEmptyIssue();
			return result.addError();
		}

		return result;
	}

	private DefinitionTargets reportAmbiguity(
			DefinitionTargets result,
			DefinitionTargets targets) {

		DefinitionTargets res = targets;

		for (DefinitionKey key : targets) {
			if (!key.isDeclaration()) {
				continue;
			}

			final DefinitionTarget previousDeclaration = result.last(key);

			if (previousDeclaration == null) {
				continue;
			}
			if (key.isValue()) {
				res = res.addError();
				getLogger().error(
						"ambiguous_value",
						logAnotherLocation(
								targets.first(key),
								previousDeclaration),
						"Ambiguous value");
				continue;
			}
			res = res.addError();
			getLogger().error(
					"ambiguous_field",
					logAnotherLocation(
							targets.first(key),
							previousDeclaration),
					"Ambiguous declaration of field '%s'",
					previousDeclaration.getFieldKey().getMemberId());
		}

		return result.add(res);
	}

	private static final class AltEnv extends DefinerEnv {

		private final DeclarativeSentence sentence;

		AltEnv(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public boolean hasPrerequisite() {
			if (this.sentence.getPrerequisite() != null) {
				return true;
			}

			final DefinerEnv initial =
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

			final DefinerEnv initial =
					this.sentence.getBlock().getInitialEnv();

			return initial.prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {

			final DefinerEnv initial =
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
		protected ValueStruct<?, ?> expectedValueStruct() {
			return this.sentence.getBlock().getInitialEnv()
					.getExpectedValueStruct();
		}

	}

}
