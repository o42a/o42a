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
package org.o42a.core;

import static org.o42a.core.AbstractContainer.parentContainerOf;

import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;


public interface Container extends ScopeInfo {

	Container getEnclosingContainer();

	default Container getParentContainer() {
		return parentContainerOf(this);
	}

	Member toMember();

	Obj toObject();

	Clause toClause();

	Namespace toNamespace();

	Member member(MemberKey memberKey);

	/**
	 * Access the member of this container.
	 *
	 * <p>This may include any member of the same scope.</p>
	 *
	 * @param access member access information.
	 * @param memberId identifier of member to access.
	 * @param declaredIn the object member declared in or <code>null</code>
	 * if unknown.
	 *
	 * @return member path or <code>null</code> if member not found.
	 */
	MemberPath member(Access access, MemberId memberId, Obj declaredIn);

	/**
	 * Determines whether this container matches the given identifier and
	 * placement and returns the member path leading to this container if so.
	 *
	 * <p>This method can call from {@link Member#matchingPath(MemberId, Obj)}
	 * if this container is a member.</p>
	 *
	 * @param memberId target member identifier or <code>null</code>
	 * to match unconditionally.
	 * @param declaredIn the object this member should be declared in
	 * or <code>null</code> to match against identifier only.
	 *
	 * @return a path to this container or <code>null</code> if it does not
	 * match the request.
	 */
	MemberPath matchingPath(MemberId memberId, Obj declaredIn);

	/**
	 * Searches for the member in current container.
	 *
	 * <p>In contrast to {@link #member(Access, MemberId, Obj)}
	 * the result is not necessarily belongs to this container. For example,
	 * it may return a path to member from used namespace or to clause inside
	 * a group.</p>
	 *
	 * @param access member access information.
	 * @param memberId identifier of member to find.
	 * @param declaredIn the object member declared in or <code>null</code>
	 * if unknown.
	 *
	 * @return member path or <code>null</code> if member not found.
	 */
	MemberPath findMember(Access access, MemberId memberId, Obj declaredIn);

}
