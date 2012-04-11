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

import static org.o42a.core.st.CommandTarget.noCommand;

import org.o42a.core.Container;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.imperative.BlockCommandEnv;
import org.o42a.core.st.impl.imperative.EllipsisStatement;


public class Imperatives extends Statements<Imperatives, Command> {

	private CommandTarget commandTarget;

	Imperatives(
			LocationInfo location,
			ImperativeSentence sentence,
			boolean opposite) {
		super(location, sentence, opposite);
	}

	@Override
	public final ImperativeSentence getSentence() {
		return (ImperativeSentence) super.getSentence();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return getSentence().getSentenceFactory();
	}

	public CommandTarget getCommandTarget() {
		if (this.commandTarget != null) {
			return this.commandTarget;
		}

		executeInstructions();

		CommandTarget result = noCommand();

		for (Command command : getImplications()) {
			result = result.combine(command.getCommandTarget());
		}

		return this.commandTarget = result;
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
		if (getSentence().isIssue()) {
			getLogger().error(
					"prohibited_issue_ellipsis",
					location,
					"Ellipsis is prohibited within issue");
			return;
		}

		final Block<?, ?> block = blockByName(location, name);

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
	protected Command define(Statement statement) {
		final ImplicationEnv initialEnv =
				getSentence().getBlock().getInitialEnv();
		return statement.command(new BlockCommandEnv(this, initialEnv));
	}

	private Block<?, ?> blockByName(LocationInfo location, String name) {
		if (name == null) {
			return getSentence().getBlock();
		}

		Block<?, ?> block = getSentence().getBlock();

		for (;;) {
			if (name.equals(block.getName())) {
				return block;
			}

			final Statements<?, ?> enclosing = block.getEnclosing();

			if (enclosing == null) {
				getLogger().unresolved(location, name);
				return null;
			}
		}
	}

}
