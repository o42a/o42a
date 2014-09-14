/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.util.fn.Init.init;

import org.o42a.core.member.Member;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Init;


public abstract class OverriddenMemberClause<C extends Clause>
		extends MemberClause {

	private final MemberClause propagatedFrom;
	private final Init<C> clause = init(this::propagateClause);

	public OverriddenMemberClause(Obj owner, MemberClause propagatedFrom) {
		super(owner, propagatedFrom);
		this.propagatedFrom = propagatedFrom;
	}

	@Override
	public final Member getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public final C clause() {
		return this.clause.get();
	}

	protected abstract C propagateClause(C propagatedFrom);

	private C propagateClause() {

		@SuppressWarnings("unchecked")
		final C propagatedFrom =
				(C) getPropagatedFrom().toClause().clause();

		return propagateClause(propagatedFrom);
	}

}
