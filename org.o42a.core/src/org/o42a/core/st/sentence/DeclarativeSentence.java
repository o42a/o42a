/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;
import static org.o42a.core.st.Definer.noDefs;
import static org.o42a.core.st.impl.SentenceErrors.declarationNotAlone;

import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.value.Condition;
import org.o42a.core.value.ValueRequest;


public abstract class DeclarativeSentence
		extends Sentence<Declaratives, Definer> {

	private final AltEnv altEnv = new AltEnv(this);
	private DefTargets targets;
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
		return this.targets = addSentenceTargets(
				prerequisiteTargets().add(altTargets()));
	}

	public final boolean isIgnored() {
		return this.ignored;
	}

	public final void ignore() {
		this.ignored = true;
	}

	public DefValue value(Resolver resolver) {

		final DeclarativeSentence prerequisite = getPrerequisite();

		if (prerequisite != null) {

			final DefValue prereqValue = prerequisite.value(resolver);

			assert !prereqValue.hasValue() :
				"Prerequisite can not have a value";

			final Condition condition = prereqValue.getCondition();

			if (!condition.isTrue()) {
				return condition.negate().toDefValue();
			}
		}

		DefValue result = TRUE_DEF_VALUE;

		for (Declaratives alt : getAlternatives()) {

			final DefValue value = alt.value(resolver);

			if (value.hasValue()) {
				return value;
			}

			final Condition condition = value.getCondition();

			if (!condition.isTrue()) {
				if (condition.isFalse()) {
					result = value;
					continue;
				}
				return value;
			}
		}

		return result;
	}

	final CommandEnv getAltEnv() {
		return this.altEnv;
	}

	private DefTargets prerequisiteTargets() {

		final DeclarativeSentence prerequisite = getPrerequisite();

		if (prerequisite == null) {
			return noDefs();
		}

		return prerequisite.getDefTargets().toPrerequisites();
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
					result = result.addError();
				}
				continue;
			} else if (!result.defining()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), result);
					result = result.addError();
				}
				continue;
			} else if (!targets.defining()) {
				if (!result.haveError()) {
					declarationNotAlone(getLogger(), targets);
					result = result.addError();
				}
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

	private static final class AltEnv extends CommandEnv {

		private final DeclarativeSentence sentence;

		AltEnv(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public ValueRequest getValueRequest() {
			return this.sentence.getBlock().getInitialEnv().getValueRequest();
		}

		@Override
		public String toString() {
			if (this.sentence == null) {
				return super.toString();
			}
			return this.sentence.toString();
		}

	}

}
