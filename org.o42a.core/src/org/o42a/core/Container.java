/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.core.artifact.Artifact;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;


public interface Container extends ScopeInfo {

	Container getEnclosingContainer();

	Container getParentContainer();

	Member toMember();

	Artifact<?> toArtifact();

	Obj toObject();

	Clause toClause();

	LocalScope toLocal();

	Namespace toNamespace();

	Member member(MemberKey memberKey);

	/**
	 * Access member of this container.
	 *
	 * <p>This includes access to object adapter's field.</p>
	 *
	 * @param user user accessing this container.
	 * @param accessor accessor of this container.
	 * @param memberId identifier of member to access.
	 * @param declaredIn the object member declared in or <code>null</code>
	 * if unknown.
	 *
	 * @return member path or <code>null</code> if member not found.
	 */
	Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn);

	/**
	 * Searches for the member in current container.
	 *
	 * <p>In contrast to {@link #member(PlaceInfo, Accessor, MemberId, Obj)}
	 * the result is not necessarily belongs to this container. For example,
	 * it may return a path to member from used name space or to clause inside
	 * a group.</p>
	 *
	 * @param user user accessing this container.
	 * @param accessor accessor of this container.
	 * @param memberId identifier of member to find.
	 * @param declaredIn the object member declared in or <code>null</code>
	 * if unknown.
	 *
	 * @return member path or <code>null</code> if member not found.
	 */
	Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn);

}
