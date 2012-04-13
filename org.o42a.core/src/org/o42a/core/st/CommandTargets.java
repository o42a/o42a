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

	private static final int PREREQUISITE_MASK = 0x01;
	private static final int PRECONDITION_MASK = 0x02;
	private static final int VALUE_MASK = 0x10;
	private static final int EXIT_MASK = 0x20;
	private static final int REPEAT_MASK = 0x40;
	private static final int ERROR_MASK = 0x100;

	private static final int CONDITIONAL_MASK =
			PREREQUISITE_MASK | PRECONDITION_MASK;
	private static final int LOOPING_MASK = EXIT_MASK | REPEAT_MASK;
	private static final int BREAKING_MASK = VALUE_MASK | LOOPING_MASK;

	private static final CommandTargets NO_COMMAND = new CommandTargets();

	public static CommandTargets noCommand() {
		return NO_COMMAND;
	}

	public static CommandTargets actionCommand(LogInfo loggable) {
		return new CommandTargets(loggable, PRECONDITION_MASK);
	}

	public static CommandTargets exitCommand(LogInfo loggable) {
		return new CommandTargets(loggable, EXIT_MASK);
	}

	public static CommandTargets repeatCommand(LogInfo loggable) {
		return new CommandTargets(loggable, REPEAT_MASK);
	}

	public static CommandTargets returnCommand(LogInfo loggable) {
		return new CommandTargets(loggable, PRECONDITION_MASK | VALUE_MASK);
	}

	private final Loggable loggable;
	private final int mask;

	private CommandTargets() {
		this.loggable = null;
		this.mask = 0;
	}

	private CommandTargets(LogInfo loggable, int mask) {
		this.loggable = loggable.getLoggable();
		this.mask = mask;
	}

	private CommandTargets(CommandTargets prototype, int mask) {
		this.loggable = prototype.loggable;
		this.mask = mask;
	}

	public final boolean isEmpty() {
		return (this.mask & (~ERROR_MASK)) == 0;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	public final boolean havePrerequisite() {
		return (this.mask & PREREQUISITE_MASK) != 0;
	}

	public final boolean havePrecondition() {
		return (this.mask & PRECONDITION_MASK) != 0;
	}

	public final boolean haveValue() {
		return (this.mask & VALUE_MASK) != 0;
	}

	public final boolean haveExit() {
		return (this.mask & EXIT_MASK) != 0;
	}

	public final boolean haveRepeat() {
		return (this.mask & REPEAT_MASK) != 0;
	}

	public final boolean haveError() {
		return (this.mask & ERROR_MASK) != 0;
	}

	public final boolean conditional() {
		return (this.mask & CONDITIONAL_MASK) != 0;
	}

	public final boolean looping() {
		return (this.mask & LOOPING_MASK) != 0;
	}

	public final boolean breaking() {
		return (this.mask & BREAKING_MASK) != 0;
	}

	public final CommandTargets addPrerequisite() {
		return addMask(PREREQUISITE_MASK);
	}

	public final CommandTargets addError() {
		return addMask(ERROR_MASK);
	}

	public final CommandTargets add(CommandTargets other) {
		if (getLoggable() != null || other.getLoggable() == null) {
			return addMask(other.mask);
		}
		return other.addMask(this.mask);
	}

	public final CommandTargets override(CommandTargets other) {
		if (getLoggable() != null || other.getLoggable() == null) {
			return setMask(other.mask);
		}
		return other;
	}

	public final CommandTargets removeLooping() {
		return removeMask(LOOPING_MASK);
	}

	public final CommandTargets toPrerequisites() {
		assert !breaking() :
			"Prerequisite should not contain breaking statements";
		if (!havePrecondition()) {
			return this;
		}
		return setMask((this.mask & ~PRECONDITION_MASK) | PREREQUISITE_MASK);
	}

	public final CommandTargets toPreconditions() {
		assert !breaking() :
			"Preconditions should not contain breaking statements";
		if (!havePrerequisite()) {
			return this;
		}
		return setMask((this.mask & ~PREREQUISITE_MASK) | PRECONDITION_MASK);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append("CommandTargets[");
		if (havePrerequisite()) {
			out.append("Prerequisite");
			comma = true;
		}
		if (havePrecondition()) {
			if (comma) {
				out.append(", precondition");
			} else {
				out.append("Precondition");
				comma = true;
			}
		}
		if (haveValue()) {
			if (comma) {
				out.append(", value");
			} else {
				out.append("Value");
				comma = true;
			}
		}
		if (haveRepeat()) {
			if (comma) {
				out.append(", repeat");
			} else {
				out.append("Repeat");
				comma = true;
			}
		}
		if (haveExit()) {
			if (comma) {
				out.append(", exit");
			} else {
				out.append("Exit");
				comma = true;
			}
		}
		if (haveError()) {
			if (comma) {
				out.append(", error");
			} else {
				out.append("Error");
			}
		}

		out.append(']');

		return out.toString();
	}

	private final CommandTargets addMask(int mask) {
		return setMask(this.mask | mask);
	}

	private final CommandTargets removeMask(int mask) {
		return setMask(this.mask & (~mask));
	}

	private final CommandTargets setMask(int mask) {
		if (mask == this.mask) {
			return this;
		}
		return new CommandTargets(this, mask);
	}

}
