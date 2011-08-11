/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member.clause;


public final class ReusedClause {

	private final Clause container;
	private final Clause clause;
	private final boolean reuseContents;

	ReusedClause(boolean reuseContents) {
		this.container = null;
		this.clause = null;
		this.reuseContents = reuseContents;
	}

	ReusedClause(Clause container, Clause clause, boolean reuseContents) {
		this.container = container;
		this.clause = clause;
		this.reuseContents = reuseContents;
	}

	public final Clause getContainer() {
		return this.container;
	}

	public final boolean isObject() {
		return this.clause == null;
	}

	public final Clause getClause() {
		return this.clause;
	}

	public final boolean reuseContents() {
		return this.reuseContents;
	}

	@Override
	public String toString() {
		if (this.clause == null) {
			if (this.reuseContents) {
				return "ReusedClause[*]";
			}
			return "ReusedClause[$object$]";
		}

		final StringBuilder out = new StringBuilder();

		out.append("ReusedClause[").append(this.clause);
		if (this.reuseContents) {
			out.append("*]");
		} else {
			out.append(']');
		}

		return out.toString();
	}

}
