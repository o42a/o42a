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
package org.o42a.core.member.local;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.util.use.UserInfo;


public abstract class MemberLocal extends Member {

	MemberLocal(LocationInfo location, Distributor distributor, Obj owner) {
		super(location, distributor);
	}

	@Override
	public final MemberField toMemberField() {
		return null;
	}

	@Override
	public final MemberClause toMemberClause() {
		return null;
	}

	@Override
	public final MemberLocal toMemberLocal() {
		return this;
	}

	@Override
	public final Field<?> toField(UserInfo user) {
		return null;
	}

	@Override
	public final Clause toClause() {
		return toLocal(dummyUser()).toClause();
	}

	@Override
	public final LocalScope substance(UserInfo user) {
		return toLocal(user);
	}

	@Override
	public final Visibility getVisibility() {

		final Clause clause = toClause();

		if (clause != null) {
			return Visibility.PUBLIC;
		}

		return Visibility.PRIVATE;
	}

	@Override
	public final boolean isOverride() {
		return isPropagated();
	}

	@Override
	public final boolean isAbstract() {
		return false;
	}

	@Override
	public final Member propagateTo(Scope scope) {

		final Obj owner = scope.getContainer().toObject();

		assert owner != null :
			scope + " is not object";

		return toLocal(scope.newResolver(dummyUser()))
		.propagateTo(owner).toMember();
	}

	@Override
	public void resolveAll() {
		toLocal(dummyUser()).resolveAll();
	}

	@Override
	public Member wrap(UserInfo user, Member inherited, Container container) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void merge(Member member) {
		throw new UnsupportedOperationException();
	}

}
