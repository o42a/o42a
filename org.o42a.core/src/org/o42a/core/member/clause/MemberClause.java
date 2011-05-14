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

import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.util.use.UserInfo;


public abstract class MemberClause extends Member {

	private final ClauseDeclaration declaration;
	private MemberKey key;

	public MemberClause(MemberOwner owner, ClauseDeclaration declaration) {
		super(declaration, declaration.distribute(), owner);
		this.declaration = declaration;
	}

	MemberClause(MemberOwner owner, MemberClause overridden) {
		super(overridden, overridden.distributeIn(owner.getContainer()), owner);
		this.key = overridden.getKey();
		this.declaration = overridden.declaration.overrideBy(this);
	}

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = getDeclaration().getMemberId().key(getScope());
	}

	@Override
	public MemberId getId() {
		return this.declaration.getMemberId();
	}

	@Override
	public final MemberField toMemberField() {
		return null;
	}

	@Override
	public final MemberClause toMemberClause() {
		return this;
	}

	@Override
	public final MemberLocal toMemberLocal() {
		return null;
	}

	@Override
	public final Field<?> toField(UserInfo user) {
		return null;
	}

	@Override
	public final LocalScope toLocal(UserInfo user) {
		return null;
	}

	@Override
	public final Visibility getVisibility() {
		return Visibility.PUBLIC;
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
	public Container substance(UserInfo user) {
		return toClause().getContainer();
	}

	@Override
	public Member getPropagatedFrom() {
		return null;
	}

	@Override
	public Member propagateTo(MemberOwner owner) {
		return toClause().propagate(owner).toMember();
	}

	@Override
	public Member wrap(MemberOwner owner, UserInfo user, Member inherited) {
		switch (getDeclaration().getKind()) {
		case GROUP:
			return new GroupClauseWrap(
					owner,
					inherited.toClause().toGroupClause(),
					toClause().toGroupClause()).toMember();
		case EXPRESSION:
		case OVERRIDER:
			return new PlainClauseWrap(
					owner,
					inherited.toClause().toPlainClause(),
					toClause().toPlainClause()).toMember();
		}

		throw new IllegalStateException(
				"Can not wrap " + getDeclaration().getKind());
	}

	@Override
	public void resolveAll() {
		toClause().resolveAll();
	}

	@Override
	protected void merge(Member member) {
		throw new IllegalStateException();
	}

}
