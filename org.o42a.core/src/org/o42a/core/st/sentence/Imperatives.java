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
import static org.o42a.core.st.CommandTargets.noCommand;

import org.o42a.core.Container;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.imperative.BlockCommandEnv;
import org.o42a.core.st.impl.imperative.EllipsisStatement;
import org.o42a.util.log.Loggable;


public final class Imperatives extends Statements<Imperatives, Command> {

	private CommandTargets commandTargets;

	Imperatives(
			LocationInfo location,
			ImperativeSentence sentence,
			Imperatives oppositeOf,
			boolean inhibit) {
		super(location, sentence, oppositeOf, inhibit);
	}

	@Override
	public final ImperativeSentence getSentence() {
		return (ImperativeSentence) super.getSentence();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public CommandTargets getCommandTargets() {
		if (this.commandTargets != null) {
			return this.commandTargets;
		}

		executeInstructions();

		final Imperatives inhibit = getOppositeOf();
		final CommandTargets inhibitTargets;

		if (inhibit == null) {
			inhibitTargets = noCommand();
		} else {
			inhibitTargets = inhibit.getCommandTargets();
			if (inhibitTargets.isEmpty()) {
				return this.commandTargets = inhibitTargets;
			}
			if (inhibitTargets.looping() && !inhibitTargets.conditional()) {
				if (inhibitTargets.haveError()) {
					return this.commandTargets = inhibitTargets;
				}

				final Loggable location =
						logAnotherLocation(getLoggable(), inhibitTargets);

				if (inhibitTargets.haveExit()) {
					getLogger().error(
							"unreachable_opposite_after_exit",
							location,
							"Opposite is unreachable, because it follows"
							+ " the unconditional loop exit");
				} else {
					getLogger().error(
							"unreachable_opposite_after_repeat",
							location,
							"Opposite is unreachable, because it follows"
							+ " the unconditional loop repeat");
				}

				return this.commandTargets = inhibitTargets.addError();
			}
		}

		return this.commandTargets =
				applyInhibitTargets(inhibitTargets, commandTargets());
	}

	@Override
	public final ImperativeBlock parentheses(LocationInfo location) {
		return parentheses(location, getContainer());
	}

	@Override
	public final ImperativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return (ImperativeBlock) super.parentheses(location, container);
	}

	@Override
	public void ellipsis(LocationInfo location, String name) {
		if (isInsideIssue()) {
			getLogger().error(
					"prohibited_issue_ellipsis",
					location,
					"Ellipsis is prohibited within issue");
			dropStatement();
			return;
		}

		final ImperativeBlock block = blockByName(location, name);

		if (block == null) {
			return;
		}
		statement(new EllipsisStatement(location, this, name));
	}

	@Override
	public void include(LocationInfo location, String name) {
		getLogger().error(
				"prohibited_imperative_inclusion",
				location,
				"Inclusion into imperative block is not possible");
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(braces);
	}

	@Override
	protected Command implicate(Statement statement) {
		final ImplicationEnv initialEnv =
				getSentence().getBlock().getInitialEnv();
		return statement.command(new BlockCommandEnv(this, initialEnv));
	}

	private ImperativeBlock blockByName(LocationInfo location, String name) {
		if (name == null) {
			return getSentence().getBlock();
		}

		ImperativeBlock block = getSentence().getBlock();

		for (;;) {
			if (name.equals(block.getName())) {
				return block;
			}

			final Statements<?, ?> enclosing = block.getEnclosing();

			if (enclosing == null) {
				break;
			}
			block = enclosing.getSentence().getBlock().toImperativeBlock();
			if (block == null) {
				break;
			}
		}

		getLogger().error(
				"unresolved_block",
				location,
				"Imperative block with name '%' does not exist",
				name);

		return null;
	}

	private CommandTargets commandTargets() {

		CommandTargets result = noCommand();
		CommandTargets prev = noCommand();

		for (Command command : getImplications()) {

			final CommandTargets targets = command.getCommandTargets();

			if (!prev.breaking() || prev.havePrerequisite()) {
				if (targets.breaking()) {
					prev = targets;
				} else {
					prev = targets.toPreconditions();
				}
				result = result.add(prev);
				continue;
			}
			if (result.haveError()) {
				continue;
			}
			result = result.addError();

			final Loggable location = logAnotherLocation(targets, prev);

			if (prev.haveExit()) {
				getLogger().error(
						"unreachable_command_after_exit",
						location,
						"Command is unreachable,"
						+ " because it follows the uncondition loop exit");
			} else if (prev.haveRepeat()) {
				getLogger().error(
						"unreachable_command_after_repeat",
						location,
						"Command is unreachable,"
						+ " because it follows the unconditional loop repeat");
			} else {
				getLogger().error(
						"unreachable_command_after_return",
						location,
						"Command is unreachable,"
						+ " because it follows the unconditional return");
			}
		}

		if (isInhibit() && result.isEmpty() && !result.haveError()) {
			getLogger().error(
					"prohibited_empty_inhibit",
					this,
					"Empty enhibit");
			return result.addError();
		}

		return result;
	}

	private CommandTargets applyInhibitTargets(
			CommandTargets inhibitTargets,
			CommandTargets commandTargets) {
		if (getOppositeOf() == null) {
			return commandTargets;
		}

		final boolean mayBeNonBreaking =
				(inhibitTargets.breaking() || commandTargets.breaking())
				&& inhibitTargets.breaking() != commandTargets.breaking();
		final CommandTargets result = inhibitTargets.add(commandTargets);

		if (mayBeNonBreaking) {
			return result.addPrerequisite();
		}

		return result;
	}

}
