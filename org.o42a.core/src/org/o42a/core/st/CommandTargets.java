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

import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


public final class CommandTargets implements LogInfo {

	private static final byte EMPTY_CODE = 0;
	private static final byte ACTION_CODE = 1;
	private static final byte LOOP_BREAK_CODE = 2;
	private static final byte RETURN_CODE = 3;

	private static final CommandTargets NO_COMMAND = new CommandTargets();

	public static CommandTargets noCommand() {
		return NO_COMMAND;
	}

	public static CommandTargets actionCommand(LogInfo loggable) {
		return new CommandTargets(loggable, ACTION_CODE);
	}

	public static CommandTargets loopBreakCommand(LogInfo loggable) {
		return new CommandTargets(loggable, LOOP_BREAK_CODE);
	}

	public static CommandTargets returnCommand(LogInfo loggable) {
		return new CommandTargets(loggable, RETURN_CODE);
	}

	private final Loggable loggable;
	private final byte code;
	private final byte howMany;
	private final boolean error;

	private CommandTargets() {
		this.loggable = null;
		this.code = EMPTY_CODE;
		this.error = false;
		this.howMany = 0;
	}

	private CommandTargets(LogInfo loggable, byte code) {
		this.loggable = loggable.getLoggable();
		this.code = code;
		this.error = false;
		this.howMany = 1;
	}

	private CommandTargets(
			CommandTargets prototype,
			byte howMany,
			boolean error) {
		this.loggable = prototype.loggable;
		this.code = prototype.code;
		this.error = error;
		this.howMany = howMany;
	}

	private CommandTargets(
			CommandTargets prototype,
			byte code,
			byte howMany,
			boolean error) {
		this.loggable = prototype.loggable;
		this.code = code;
		this.howMany = howMany;
		this.error = error;
	}

	public final boolean isEmpty() {
		return this.code == EMPTY_CODE;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	public final boolean haveMany() {
		return this.howMany > 1;
	}

	public final boolean haveBlockExit() {
		return this.code >= LOOP_BREAK_CODE;
	}

	public final boolean haveReturn() {
		return this.code == RETURN_CODE;
	}

	public final boolean haveError() {
		return this.error;
	}

	public final CommandTargets addError() {
		if (haveError()) {
			return this;
		}
		return new CommandTargets(this, this.howMany, true);
	}

	public final CommandTargets add(CommandTargets other) {

		final CommandTargets selected;

		if (other.code > this.code) {
			selected = other;
		} else {
			selected = this;
		}

		final CommandTargets result =
				selected.setAmount(this.howMany + other.howMany);

		if (!haveError() && !other.haveError()) {
			return result;
		}

		return result.addError();
	}

	public final CommandTargets set(CommandTargets other) {

		final CommandTargets result;

		if (other.code >= this.code) {
			result = other;
		} else {
			result = setHowMany(other.howMany);
		}

		if (!haveError() && !other.haveError()) {
			return result;
		}

		return result.addError();
	}

	public final CommandTargets removeLoopBreaks() {
		if (this.code != LOOP_BREAK_CODE) {
			return this;
		}
		return new CommandTargets(this, ACTION_CODE, this.howMany, haveError());
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "NoCommands";
		}

		final String prefix;

		switch (this.code) {
		case ACTION_CODE:
			prefix = "Action";
			break;
		case LOOP_BREAK_CODE:
			prefix = "Break";
			break;
		case RETURN_CODE:
			prefix = "Return";
			break;
		default:
			prefix = "Unknown";
		}

		return (prefix
				+ (haveMany() ? "Commands[" : "Command[")
				+ this.loggable + ']');
	}

	private final CommandTargets setAmount(int amount) {

		final byte howMany;

		if (amount == 0) {
			howMany = 0;
		} else if (amount == 1) {
			howMany = 1;
		} else {
			howMany = 2;
		}

		return setHowMany(howMany);
	}

	private final CommandTargets setHowMany(byte howMany) {
		if (this.howMany == howMany) {
			return this;
		}
		return new CommandTargets(this, howMany, haveError());
	}

}
