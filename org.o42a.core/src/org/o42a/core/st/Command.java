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
package org.o42a.core.st;

import static org.o42a.core.st.CommandTargets.NO_COMMANDS;
import static org.o42a.core.st.ImplicationTargets.*;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ref.*;
import org.o42a.core.st.action.Action;
import org.o42a.util.log.LogInfo;


public abstract class Command extends Implication<Command> {

	public static CommandTargets noCommands() {
		return NO_COMMANDS;
	}

	public static CommandTargets exitCommand(LogInfo loggable) {
		return new CommandTargets(loggable, EXIT_MASK);
	}

	private final CommandEnv env;

	public Command(Statement statement, CommandEnv env) {
		super(statement);
		this.env = env;
	}

	public abstract CommandTargets getCommandTargets();

	public final CommandEnv env() {
		return this.env;
	}

	public abstract Action initialValue(Resolver resolver);

	public abstract Action initialCond(Resolver resolver);

	public final void resolveAll(FullResolver resolver) {
		getStatement().fullyResolved();
		getContext().fullResolution().start();
		try {
			fullyResolve(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public abstract InlineCmd inline(Normalizer normalizer, Scope origin);

	public abstract InlineCmd normalize(
			RootNormalizer normalizer,
			Scope origin);

	public abstract Cmd cmd();

	protected abstract void fullyResolve(FullResolver resolver);

	protected final CommandTargets actionCommand() {
		return new CommandTargets(
				this,
				PRECONDITION_MASK | NON_CONSTANT_MASK);
	}

	protected final CommandTargets exitCommand() {
		return new CommandTargets(this, EXIT_MASK);
	}

	protected final CommandTargets repeatCommand() {
		return new CommandTargets(this, REPEAT_MASK);
	}

	protected final CommandTargets returnCommand() {
		return new CommandTargets(
				this,
				PRECONDITION_MASK | VALUE_MASK | NON_CONSTANT_MASK);
	}

}
