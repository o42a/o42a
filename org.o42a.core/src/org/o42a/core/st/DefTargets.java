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


public final class DefTargets extends ImplicationTargets<DefTargets> {

	static final DefTargets NO_DEFS = new DefTargets();

	private DefTargets() {
	}

	DefTargets(LogInfo loggable, int mask) {
		super(loggable, mask);
	}

	DefTargets(LocationInfo location, int mask) {
		super(location.getLocation(), mask);
	}

	public final boolean isClaim() {
		return (mask() & CLAIM_MASK) != 0;
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

	public final DefTargets claim() {
		return setMask(CLAIM_MASK);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append("DefTargets[");
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
	protected DefTargets create(int mask) {
		return new DefTargets(this, mask);
	}

}
