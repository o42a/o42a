/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.st.impl.local;

import static org.o42a.core.member.AccessSource.FROM_DECLARATION;
import static org.o42a.core.member.AccessSource.FROM_DEFINITION;

import org.o42a.core.*;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.st.sentence.Local;


class LocalInsides extends AbstractContainer {

	private final Local local;

	public LocalInsides(Local local) {
		super(local);
		this.local = local;
	}

	public final Local getLocal() {
		return this.local;
	}

	@Override
	public final Container getEnclosingContainer() {
		return getLocal().getContainer();
	}

	@Override
	public final Scope getScope() {
		return getEnclosingContainer().getScope();
	}

	@Override
	public final Member toMember() {
		return getEnclosingContainer().toMember();
	}

	@Override
	public final Obj toObject() {
		return getEnclosingContainer().toObject();
	}

	@Override
	public final Clause toClause() {
		return getEnclosingContainer().toClause();
	}

	@Override
	public final Namespace toNamespace() {
		return null;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return getEnclosingContainer().member(memberKey);
	}

	@Override
	public MemberPath member(Access access, MemberId memberId, Obj declaredIn) {

		final Local local = local(access, memberId, declaredIn);

		if (local != null) {
			return local;
		}

		return getEnclosingContainer()
				.member(access, memberId, declaredIn);
	}

	@Override
	public MemberPath findMember(
			Access access,
			MemberId memberId,
			Obj declaredIn) {

		final Local local = local(access, memberId, declaredIn);

		if (local != null) {
			return local;
		}

		return getEnclosingContainer()
				.findMember(access, memberId, declaredIn);
	}

	@Override
	public String toString() {
		if (this.local == null) {
			return super.toString();
		}
		return this.local.toString();
	}

	private Local local(Access access, MemberId memberId, Obj declaredIn) {
		if (!accessibleBy(access)) {
			return null;
		}
		if (!matchLocal(memberId, declaredIn)) {
			return null;
		}
		if (!localsVisibleBy(access.getContainer(), access.getSource())) {
			return null;
		}
		return getLocal();
	}

	private boolean accessibleBy(Access access) {
		if (access.getSource() == AccessSource.FROM_TYPE) {
			return false;
		}

		switch (access.getAccessor()) {
		case DECLARATION:
		case OWNER:
		case ENCLOSED:
			return true;
		case INHERITANT:
		case PUBLIC:
		}

		return false;
	}

	private boolean localsVisibleBy(Container by, AccessSource source) {

		final Clause clause = by.toClause();

		if (clause != null) {
			return localsVisibleByClause(clause, source);
		}

		return localsVisibleByScope(by.getScope(), source);
	}

	private boolean localsVisibleByClause(Clause by, AccessSource source) {

		final Clause clause = toClause();

		if (clause == null) {
			// Clause can not see any locals declared outside top-level clause.
			return false;
		}
		if (clause == by) {
			// Access from the same clause.
			// Locals are visible only from value definition.
			return source == FROM_DEFINITION;
		}

		final Clause enclosing = clause.getEnclosingClause();

		if (enclosing == null) {
			return false;
		}

		switch (clause.getKind()) {
		case OVERRIDER:
			// A field overrider is a declaration.
			return localsVisibleByClause(enclosing, FROM_DECLARATION);
		case EXPRESSION:
		case GROUP:
			return localsVisibleByClause(enclosing, FROM_DEFINITION);
		}

		throw new IllegalStateException(
				"Unexpected kind of clause: " + clause.getKind());
	}

	private boolean localsVisibleByScope(Scope by, AccessSource source) {
		if (getScope().is(by)) {
			// Access from the same scope.
			// Locals are visible only from value definition.
			return source == FROM_DEFINITION;
		}

		final Container enclosingContainer = by.getEnclosingContainer();

		if (enclosingContainer == null) {
			return false;
		}

		final Clause enclosingClause = enclosingContainer.toClause();

		if (enclosingClause != null) {
			if (by.toField() != null) {
				// A field declared inside clauses.
				return localsVisibleByClause(enclosingClause, FROM_DECLARATION);
			}
			// An expression inside clause.
			return localsVisibleByClause(enclosingClause, FROM_DEFINITION);
		}

		// Determine accessibility for enclosing scope.
		final Scope enclosing = enclosingContainer.getScope();

		if (enclosing.isTopScope()) {
			return false;
		}
		if (by.toField() != null) {
			// This scope is a field, i.e. a declaration.
			return localsVisibleByScope(enclosing, FROM_DECLARATION);
		}

		// Otherwise, this scope is declared inside a value definition.
		return localsVisibleByScope(enclosing, FROM_DEFINITION);
	}

	private boolean matchLocal(MemberId memberId, Obj declaredIn) {

		final MemberName memberName = memberId.toMemberName();

		if (memberName == null) {
			return false;
		}
		if (memberName.getEnclosingId() != null) {
			return false;
		}

		final MemberKind memberKind = memberName.getKind();

		if (memberKind == MemberKind.FIELD) {
			if (declaredIn != null) {
				// `foo @bar` is always a field lookup.
				return false;
			}
		} else if (memberKind != MemberKind.LOCAL) {
			return false;
		}
		if (!memberName.getName().is(getLocal().getName())) {
			return false;
		}
		if (declaredIn == null) {
			return true;
		}

		return getLocal().getScope().is(declaredIn.getScope());
	}

}
