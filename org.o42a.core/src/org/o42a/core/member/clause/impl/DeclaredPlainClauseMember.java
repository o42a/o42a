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
package org.o42a.core.member.clause.impl;

import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.MemberClause;


final class DeclaredPlainClauseMember extends MemberClause {

	final DeclaredPlainClause clause;

	DeclaredPlainClauseMember(ClauseBuilder builder) {
		super(builder.getMemberOwner(), builder.getDeclaration());
		this.clause = new DeclaredPlainClause(this, builder);
	}

	@Override
	public final DeclaredPlainClause clause() {
		return this.clause;
	}

	@Override
	public MemberClause propagateTo(MemberOwner owner) {
		return new Propagated(owner, this);
	}

	private static final class Propagated
			extends OverriddenMemberClause<DeclaredPlainClause> {

		private Propagated(MemberOwner owner, MemberClause propagatedFrom) {
			super(owner, propagatedFrom);
		}

		@Override
		public MemberClause propagateTo(MemberOwner owner) {
			return new Propagated(owner, this);
		}

		@Override
		protected DeclaredPlainClause propagateClause(
				DeclaredPlainClause propagatedFrom) {
			return new DeclaredPlainClause(this, propagatedFrom);
		}

	}

}
