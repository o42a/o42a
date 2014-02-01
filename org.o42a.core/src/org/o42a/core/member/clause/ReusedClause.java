/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.core.member.Visibility;


public final class ReusedClause {

	public static final ReusedClause REUSE_OBJECT = new ReusedClause();

	private final Clause container;
	private final MemberClause clause;
	private final boolean reuseContents;

	private ReusedClause() {
		this.container = null;
		this.clause = null;
		this.reuseContents = true;
	}

	public ReusedClause(
			Clause container,
			MemberClause clause,
			boolean reuseContents) {
		this.container = container;
		this.clause = clause;
		this.reuseContents = validateContentReuse(reuseContents);
	}

	public final Clause getContainer() {
		return this.container;
	}

	public final boolean isObject() {
		return this.clause == null;
	}

	public final MemberClause getClause() {
		return this.clause;
	}

	public final boolean reuseContents() {
		return this.reuseContents;
	}

	@Override
	public String toString() {
		if (this.clause == null) {
			return "ReusedClause[*]";
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

	private boolean validateContentReuse(boolean reuseContents) {
		if (this.clause.getVisibility() == Visibility.PRIVATE) {
			return true;
		}
		if (reuseContents && !this.clause.clause().hasContinuation()) {
			this.clause.getLogger().error(
					"empty_clause_contents_reused",
					this.clause,
					"Attempt to reuse contents of empty clause");
		}
		return reuseContents;
	}

}
