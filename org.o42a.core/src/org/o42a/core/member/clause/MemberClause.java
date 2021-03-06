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

import static org.o42a.util.fn.Init.init;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Init;


public abstract class MemberClause extends NonAliasMember {

	private final ClauseDeclaration declaration;
	private final Init<MemberId> memberId =
			init(() -> getDeclaration().getMemberId());
	private final Init<MemberKey> key =
			init(() -> getMemberId().key(getScope()));

	public MemberClause(Obj owner, ClauseDeclaration declaration) {
		super(declaration, declaration.distribute(), owner);
		this.declaration = declaration;
	}

	protected MemberClause(Obj owner, MemberClause propagatedFrom) {
		super(
				owner.getLocation().setDeclaration(
						propagatedFrom.getLastDefinition()),
				propagatedFrom.distributeIn(owner),
				owner);
		this.key.set(propagatedFrom.getMemberKey());
		this.declaration = propagatedFrom.declaration.overrideBy(this);
	}

	@Override
	public final MemberId getMemberId() {
		return this.memberId.get();
	}

	@Override
	public final MemberKey getMemberKey() {
		return this.key.get();
	}

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	public final ClauseKind getKind() {
		return getDeclaration().getKind();
	}

	public final boolean isImplicit() {
		return getDeclaration().isImplicit();
	}

	public abstract Clause clause();

	@Override
	public final MemberTypeParameter toTypeParameter() {
		return null;
	}

	@Override
	public final MemberField toField() {
		return null;
	}

	@Override
	public final MemberLocal toLocal() {
		return null;
	}

	@Override
	public final MemberClause toClause() {
		return this;
	}

	@Override
	public final Visibility getVisibility() {
		if (getDeclaration().isImplicit() || getDeclaration().isInternal()) {
			return Visibility.PRIVATE;
		}
		return Visibility.PUBLIC;
	}

	@Override
	public final boolean isOverride() {
		return isPropagated();
	}

	@Override
	public final Container substance(UserInfo user) {
		return clause().getContainer();
	}

	@Override
	public Member getPropagatedFrom() {
		return null;
	}

	@Override
	public abstract MemberClause propagateTo(Obj owner);

	@Override
	public void resolveAll() {
		clause().resolveAll();
	}

}
