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

import static org.o42a.core.st.CommandTargets.loopBreakCommand;
import static org.o42a.core.st.CommandTargets.noCommand;
import static org.o42a.core.st.impl.imperative.BlockCommand.reportUnreachable;

import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandTargets;


public abstract class ImperativeSentence
		extends Sentence<Imperatives, Command> {

	private CommandTargets commandTargets;

	protected ImperativeSentence(
			LocationInfo location,
			ImperativeBlock block,
			ImperativeFactory sentenceFactory) {
		super(location, block, sentenceFactory);
	}

	@Override
	public final ImperativeBlock getBlock() {
		return (ImperativeBlock) super.getBlock();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@Override
	public final ImperativeSentence getPrerequisite() {
		return (ImperativeSentence) super.getPrerequisite();
	}

	public CommandTargets getCommandTargets() {
		if (this.commandTargets != null) {
			return this.commandTargets;
		}

		final CommandTargets prerequisiteTarget;
		final ImperativeSentence prerequisite = getPrerequisite();

		if (prerequisite == null) {
			prerequisiteTarget = noCommand();
		} else {
			prerequisiteTarget = prerequisite.getCommandTargets();
			if (prerequisiteTarget.isEmpty()) {
				return this.commandTargets = prerequisiteTarget;
			}
		}

		return this.commandTargets = applyClaimTargets(
				prerequisiteTarget.add(altTargets()));
	}

	private CommandTargets altTargets() {

		CommandTargets result = noCommand();

		for (Imperatives alt : getAlternatives()) {

			final CommandTargets targets = alt.getCommandTargets();

			if (targets.isEmpty()) {
				continue;
			}
			if (result.haveBlockExit() && !result.haveMany()) {
				if (!result.haveError()) {
					reportUnreachable(getLogger(), result, targets);
					result = result.addError();
				}
			} else {
				result = result.set(targets);
			}
		}
		if (isIssue() && result.isEmpty() && !result.haveError()) {
			reportEmptyIssue();
			return result.addError();
		}

		return result;
	}

	private CommandTargets applyClaimTargets(CommandTargets targets) {
		if (!isClaim()) {
			return targets;
		}

		final CommandTargets breakCommand = loopBreakCommand(this);

		if (getPrerequisite() != null) {
			return targets.add(breakCommand);
		}

		return targets.set(breakCommand);
	}

}
