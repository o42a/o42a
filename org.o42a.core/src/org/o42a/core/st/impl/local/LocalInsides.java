/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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

import org.o42a.core.*;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.Local;


public class LocalInsides extends AbstractContainer {

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
	public final LocalScope toLocalScope() {
		return null;
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
	public Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {

		final Path local = local(accessor, memberId, declaredIn);

		if (local != null) {
			return local;
		}

		return getEnclosingContainer()
				.member(user, accessor, memberId, declaredIn);
	}

	@Override
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {

		final Path local = local(accessor, memberId, declaredIn);

		if (local != null) {
			return local;
		}

		return getEnclosingContainer()
				.findMember(user, accessor, memberId, declaredIn);
	}

	@Override
	public String toString() {
		if (this.local == null) {
			return super.toString();
		}
		return this.local.toString();
	}

	private Path local(Accessor accessor, MemberId memberId, Obj declaredIn) {
		if (accessibleBy(accessor) && matchLocal(memberId, declaredIn)) {
			return getLocal().toPath();
		}
		return null;
	}

	private boolean accessibleBy(Accessor accessor) {
		return accessor == Accessor.DECLARATION
				|| accessor == Accessor.OWNER
				|| accessor == Accessor.ENCLOSED;
	}

	private boolean matchLocal(MemberId memberId, Obj declaredIn) {
		if (declaredIn != null) {
			return false;
		}

		final MemberName memberName = memberId.toMemberName();

		if (memberName == null) {
			return false;
		}
		if (memberName.getEnclosingId() != null) {
			return false;
		}
		if (memberName.getKind() != MemberKind.FIELD) {
			return false;
		}

		return memberName.getName().is(getLocal().getName());
	}

}
