/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.member.impl.clause;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ReusedClause;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;


public final class ReusedClauseRef {

	private final Ref ref;
	private boolean reuseContents;

	public ReusedClauseRef() {
		this.ref = null;
		this.reuseContents = true;
	}

	public ReusedClauseRef(Ref ref, boolean reuseContents) {
		this.ref = ref;
		this.reuseContents = reuseContents;
	}

	public ReusedClause reuse(Clause clause) {
		if (this.ref == null) {
			return new ReusedClause();
		}

		final ClauseReuser reuser =
				new ClauseReuser(this.ref, this.reuseContents);
		final Resolver resolver =
				clause.getEnclosingScope().walkingResolver(dummyUser(), reuser);

		if (!this.ref.resolve(resolver).isResolved()) {
			return null;
		}

		return reuser.getReused();
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return "*";
		}
		if (!this.reuseContents) {
			return this.ref.toString();
		}
		return this.ref + "*";
	}

}
