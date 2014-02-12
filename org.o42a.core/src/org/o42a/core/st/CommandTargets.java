/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


public final class CommandTargets implements LogInfo {

	static final CommandTargets NO_COMMANDS = new CommandTargets();

	static final int NON_CONSTANT_MASK = 0x01;
	static final int PREREQUISITE_MASK = 0x02;
	static final int PRECONDITION_MASK = 0x04;
	static final int VALUE_MASK = 0x08;
	static final int FIELD_MASK = 0x10;
	static final int CLAUSE_MASK = 0x20;
	static final int EXIT_MASK = 0x100;
	static final int REPEAT_MASK = 0x200;
	static final int ERROR_MASK = 0x1000;

	static final int CONDITIONAL_MASK = PREREQUISITE_MASK | PRECONDITION_MASK;
	static final int DECLARING_MASK = FIELD_MASK | CLAUSE_MASK;
	static final int LOOPING_MASK = EXIT_MASK | REPEAT_MASK;
	static final int BREAKING_MASK = VALUE_MASK | LOOPING_MASK;
	static final int DEFINITION_MASK =
			CONDITIONAL_MASK | VALUE_MASK | LOOPING_MASK;

	private final Loggable loggable;
	private final int mask;

	private CommandTargets() {
		this.loggable = null;
		this.mask = 0;
	}

	CommandTargets(LogInfo loggable, int mask) {
		this.loggable = loggable.getLoggable();
		this.mask = mask;
	}

	CommandTargets(LocationInfo location, int mask) {
		this.loggable = location.getLocation().getLoggable();
		this.mask = mask;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	public final boolean isEmpty() {
		return (mask() & ~ERROR_MASK) == 0;
	}

	public final boolean isConstant() {
		return (mask() & NON_CONSTANT_MASK) == 0;
	}

	public final boolean haveDefinition() {
		return (mask() & DEFINITION_MASK) != 0;
	}

	public final boolean havePrerequisite() {
		return (mask() & PREREQUISITE_MASK) != 0;
	}

	public final boolean havePrecondition() {
		return (mask() & PRECONDITION_MASK) != 0;
	}

	public final boolean haveValue() {
		return (mask() & VALUE_MASK) != 0;
	}

	public final boolean haveError() {
		return (mask() & ERROR_MASK) != 0;
	}

	public final boolean conditional() {
		return (mask() & CONDITIONAL_MASK) != 0;
	}

	public final boolean breaking() {
		return (mask() & BREAKING_MASK) != 0;
	}

	public final boolean unconditionallyBreaking() {
		return breaking() && !conditional();
	}

	public final CommandTargets addPrerequisite() {
		return addMask(PREREQUISITE_MASK);
	}

	public final CommandTargets addError() {
		return addMask(ERROR_MASK);
	}

	public final CommandTargets setConstant() {
		return removeMask(NON_CONSTANT_MASK);
	}

	public final CommandTargets add(CommandTargets other) {
		if (getLoggable() != null || other.getLoggable() == null) {
			return addMask(other.mask());
		}
		return other.addMask(mask());
	}

	public final CommandTargets override(CommandTargets other) {
		if (getLoggable() != null || other.getLoggable() == null) {
			return setMask(other.mask());
		}
		return other;
	}

	public final CommandTargets toPrerequisites() {
		if (!havePrecondition()) {
			return this;
		}
		return setMask((mask() & ~PRECONDITION_MASK) | PREREQUISITE_MASK);
	}

	public final CommandTargets toPreconditions() {
		if (!havePrerequisite()) {
			return this;
		}
		return setMask((mask() & ~PREREQUISITE_MASK) | PRECONDITION_MASK);
	}

	public final boolean haveField() {
		return (mask() & FIELD_MASK) != 0;
	}

	public final boolean haveClause() {
		return (mask() & CLAUSE_MASK) != 0;
	}

	public final boolean declaring() {
		return (mask() & DECLARING_MASK) != 0;
	}

	public final boolean defining() {
		return (mask() & ~DECLARING_MASK) != 0;
	}

	public final boolean haveExit() {
		return (mask() & EXIT_MASK) != 0;
	}

	public final boolean haveRepeat() {
		return (mask() & REPEAT_MASK) != 0;
	}

	public final boolean looping() {
		return (mask() & LOOPING_MASK) != 0;
	}

	public final CommandTargets removeLooping() {
		return removeMask(LOOPING_MASK);
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
		if (haveField()) {
			if (comma) {
				out.append(", field");
			} else {
				out.append("Field");
				comma = true;
			}
		}
		if (haveClause()) {
			if (comma) {
				out.append(", clause");
			} else {
				out.append("Clause");
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

	protected final int mask() {
		return this.mask;
	}

	protected final CommandTargets addMask(int mask) {
		return setMask(mask() | mask);
	}

	protected final CommandTargets removeMask(int mask) {
		return setMask(mask() & (~mask));
	}

	protected final CommandTargets setMask(int mask) {
		if (mask == mask()) {
			return this;
		}
		return new CommandTargets(this, mask);
	}

}
