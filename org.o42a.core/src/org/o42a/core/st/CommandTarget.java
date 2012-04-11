/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.core.st.DefinitionTarget.valueDefinition;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;


public final class CommandTarget extends ImplicationTarget {

	public static CommandTarget noCommand() {
		return new CommandTarget(null, (byte) 0);
	}

	public static CommandTarget actionCommand(Statement statement) {
		return new CommandTarget(statement, (byte) 1);
	}

	public static CommandTarget returnCommand(Statement statement) {
		return new CommandTarget(statement, (byte) 2);
	}

	private final Statement statement;
	private final byte command;

	CommandTarget(Statement statement, byte command) {
		this.statement = statement;
		this.command = command;
	}

	public final boolean isEmpty() {
		return this.command == 0;
	}

	public final boolean isReturn() {
		return this.command > 1;
	}

	public final Statement getStatement() {
		return this.statement;
	}

	@Override
	public final boolean haveValue() {
		return isReturn();
	}

	public final DefinitionTargets toDefinitionTargets() {
		if (isEmpty()) {
			return noDefinitions();
		}
		if (isReturn()) {
			return valueDefinition(getStatement());
		}
		return valueDefinition(getStatement());
	}

	public final CommandTarget combine(CommandTarget other) {
		return other.command > this.command ? other : this;
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "NoCommand";
		}
		if (isReturn()) {
			return "ReturnCommand[" + this.statement + ']';
		}
		return "ActionCommand[" + this.statement + ']';
	}

}
