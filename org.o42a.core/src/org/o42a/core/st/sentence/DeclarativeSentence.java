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

import static org.o42a.core.st.Implication.noCommands;
import static org.o42a.core.st.impl.SentenceErrors.declarationNotAlone;

import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.Definer;


public abstract class DeclarativeSentence
		extends Sentence<Declaratives, Definer> {

	private CommandTargets targets;
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

	@Override
	public CommandTargets getTargets() {
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

	private CommandTargets prerequisiteTargets() {

		final DeclarativeSentence prerequisite = getPrerequisite();

		if (prerequisite == null) {
			return noCommands();
		}

		return prerequisite.getTargets().toPrerequisites();
	}

	private CommandTargets altTargets() {

		CommandTargets result = noCommands();
		Declaratives first = null;

		for (Declaratives alt : getAlternatives()) {

			final CommandTargets targets = alt.getTargets();

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

	private CommandTargets addSentenceTargets(CommandTargets targets) {

		final CommandTargets result;

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

}
