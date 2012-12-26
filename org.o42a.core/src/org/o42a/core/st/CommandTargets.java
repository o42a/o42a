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

import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public final class CommandTargets extends ImplicationTargets<CommandTargets> {

	static final CommandTargets NO_COMMANDS = new CommandTargets();

	private CommandTargets() {
	}

	CommandTargets(LogInfo loggable, int mask) {
		super(loggable, mask);
	}

	CommandTargets(LocationInfo location, int mask) {
		super(location.getLocation(), mask);
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

	public final DefTargets toDefTargets() {
		return new DefTargets(this, mask() & ~LOOPING_MASK);
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

	@Override
	protected CommandTargets create(int mask) {
		return new CommandTargets(this, mask);
	}

}
