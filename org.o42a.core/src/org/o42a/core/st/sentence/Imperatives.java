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

import static org.o42a.core.st.Command.noCommands;

import org.o42a.core.Container;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.imperative.BlockCommandEnv;
import org.o42a.core.st.impl.imperative.EllipsisStatement;
import org.o42a.util.string.Name;


public final class Imperatives extends Statements<Imperatives, Command> {

	private CommandTargets commandTargets;

	Imperatives(LocationInfo location, ImperativeSentence sentence) {
		super(location, sentence);
	}

	@Override
	public final ImperativeSentence getSentence() {
		return (ImperativeSentence) super.getSentence();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return super.getSentenceFactory().toImperativeFactory();
	}

	public CommandTargets getCommandTargets() {
		if (this.commandTargets != null) {
			return this.commandTargets;
		}
		executeInstructions();
		return this.commandTargets = commandTargets();
	}

	@Override
	public final ImperativeBlock parentheses(LocationInfo location) {
		return super.parentheses(location).toImperativeBlock();
	}

	@Override
	public final ImperativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return super.parentheses(location, container).toImperativeBlock();
	}

	@Override
	public void ellipsis(LocationInfo location, Name name) {
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
		block.loop();
		statement(new EllipsisStatement(location, this, name));
	}

	@Override
	public void include(LocationInfo location, Name name) {
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

		final CommandEnv initialEnv = getSentence().getBlock().sentencesEnv();

		return statement.command(new BlockCommandEnv(initialEnv));
	}

	private ImperativeBlock blockByName(LocationInfo location, Name name) {
		if (name == null) {
			return getSentence().getBlock();
		}

		ImperativeBlock block = getSentence().getBlock();

		for (;;) {
			if (name.is(block.getName())) {
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
				"Imperative block with name '%s' does not exist",
				name);

		return null;
	}

	private CommandTargets commandTargets() {

		CommandTargets result = noCommands();
		CommandTargets prev = noCommands();

		for (Command command : getImplications()) {

			final CommandTargets targets = command.getTargets();

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
			getLogger().error(
					"unreachable_command",
					targets,
					"Unreachable command");
		}

		return result;
	}

}
