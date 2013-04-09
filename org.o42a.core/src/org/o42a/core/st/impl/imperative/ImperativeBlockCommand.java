/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.impl.cmd.BlockCommand;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.st.sentence.Imperatives;


public final class ImperativeBlockCommand
		extends BlockCommand<ImperativeBlock> {

	private CommandTargets commandTargets;

	public ImperativeBlockCommand(ImperativeBlock block, CommandEnv env) {
		super(block, env);
	}

	@Override
	public CommandTargets getTargets() {
		if (this.commandTargets != null) {
			return this.commandTargets;
		}

		getBlock().executeInstructions();

		return this.commandTargets = applyBraceTargets(sentenceTargets());
	}

	@Override
	public DefTarget toTarget(Scope origin) {

		final CommandTargets targets = getTargets();

		if (targets.isEmpty()) {
			return null;
		}
		if (targets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}
		if (targets.looping()) {
			return NO_DEF_TARGET;
		}
		if (!targets.haveValue()) {
			return NO_DEF_TARGET;
		}

		for (ImperativeSentence sentence : getBlock().getSentences()) {

			final DefTarget target = sentenceTarget(origin, sentence);

			if (target != null) {
				return target;
			}
		}

		return null;
	}

	private CommandTargets sentenceTargets() {

		CommandTargets result = noCommands();

		for (ImperativeSentence sentence : getBlock().getSentences()) {

			final CommandTargets targets = sentence.getTargets();

			if (!result.breaking() || result.havePrerequisite()) {
				if (!targets.breaking()) {
					result = result.add(targets.toPreconditions());
				} else {
					result = result.add(targets);
					if (!targets.havePrerequisite()) {
						result = result.toPreconditions();
					}
				}
				continue;
			}
			if (result.haveError()) {
				continue;
			}
			result = result.addError();
			getLogger().error(
					"unreachable_sentence",
					targets,
					"Unreachable sentence");
		}

		return result;
	}

	private CommandTargets applyBraceTargets(CommandTargets targets) {
		if (getBlock().isParentheses()) {
			return targets;
		}
		return targets.removeLooping();
	}

	private static DefTarget sentenceTarget(
			Scope origin,
			ImperativeSentence sentence) {

		final CommandTargets targets = sentence.getTargets();

		if (targets.isEmpty()) {
			return null;
		}
		if (targets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}
		if (targets.looping()) {
			return NO_DEF_TARGET;
		}
		if (!targets.haveValue()) {
			return NO_DEF_TARGET;
		}

		final List<Imperatives> alts = sentence.getAlternatives();
		final int size = alts.size();

		if (size != 1) {
			if (size == 0) {
				return null;
			}
			return NO_DEF_TARGET;
		}

		return statementsTarget(origin, alts.get(0));
	}

	private static DefTarget statementsTarget(
			Scope origin,
			Imperatives statements) {

		final CommandTargets targets = statements.getTargets();

		if (targets.isEmpty()) {
			return null;
		}
		if (targets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}
		if (targets.looping()) {
			return NO_DEF_TARGET;
		}
		if (!targets.haveValue()) {
			return NO_DEF_TARGET;
		}

		final List<Command> commands = statements.getCommands();
		final int size = commands.size();

		if (size != 1) {
			if (size == 0) {
				return null;
			}
			return commands.get(0).toTarget(origin);
		}

		return NO_DEF_TARGET;
	}

}
