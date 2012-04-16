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

import static org.o42a.core.st.CommandTargets.exitCommand;
import static org.o42a.core.st.CommandTargets.noCommand;

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
		return super.getSentenceFactory().toImperativeFactory();
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

		return this.commandTargets = applyExitTargets(
				prerequisiteTarget.toPrerequisites().add(altTargets()));
	}

	private CommandTargets altTargets() {

		CommandTargets result = noCommand();
		boolean notFirst = false;

		for (Imperatives alt : getAlternatives()) {

			final CommandTargets targets = alt.getCommandTargets();

			if (notFirst && result.isEmpty()) {
				if (!result.haveError()) {
					getLogger().error(
							"prohibited_empty_alternative",
							this,
							"Empty alternative");
				}
				return result.addError();
			}
			notFirst = true;
			if (result.conditional() || !result.looping()) {
				if (result.isEmpty()) {
					result = targets;
					continue;
				}
				if (targets.isEmpty()) {
					continue;
				}

				final boolean mayBeNonBreaking =
						(result.breaking() || targets.breaking())
						&& result.breaking() != targets.breaking();

				result = result.add(targets);
				if (mayBeNonBreaking) {
					result = result.addPrerequisite();
				}
				continue;
			}
			if (result.haveError()) {
				continue;
			}
			result = result.addError();
			getLogger().error(
					"unreachable_alternative",
					targets,
					"Unreachable alternative");
		}
		if (isIssue() && result.isEmpty() && !result.haveError()) {
			reportEmptyIssue();
			return result.addError();
		}

		return result;
	}

	private CommandTargets applyExitTargets(CommandTargets targets) {
		if (!isClaim()) {
			return targets;
		}

		final CommandTargets exit = exitCommand(this);

		if (getPrerequisite() != null) {
			return targets.add(exit);
		}

		return targets.override(exit);
	}

}
