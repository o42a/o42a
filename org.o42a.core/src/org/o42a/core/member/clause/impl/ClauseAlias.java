/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Init;


public final class ClauseAlias extends Member {

	private final MemberId memberId;
	private final ClauseAlias propagatedFrom;
	private final Init<MemberKey> memberKey =
			init(() -> getMemberId().key(getMemberOwner().getScope()));
	private final Init<MemberClause> aliasedClause =
			init(this::findAliasedClause);

	public ClauseAlias(MemberId memberId, MemberClause aliasedClause) {
		super(
				aliasedClause,
				aliasedClause.distribute(),
				aliasedClause.getMemberOwner());
		this.memberId = memberId;
		this.propagatedFrom = null;
		this.aliasedClause.set(aliasedClause);
	}

	private ClauseAlias(Obj owner, ClauseAlias prototype) {
		super(
				owner.getLocation().setDeclaration(prototype),
				prototype.distributeIn(owner.getContainer()),
				owner);
		this.memberId = prototype.getMemberId();
		this.propagatedFrom = prototype;
		this.memberKey.set(prototype.getMemberKey());
	}

	@Override
	public final MemberId getMemberId() {
		return this.memberId;
	}

	@Override
	public final MemberKey getMemberKey() {
		return this.memberKey.get();
	}

	@Override
	public MemberPath getMemberPath() {
		return getAliasedClause().getMemberPath();
	}

	@Override
	public final Container substance(UserInfo user) {
		return getAliasedClause().substance(user);
	}

	@Override
	public Visibility getVisibility() {
		return Visibility.PRIVATE;
	}

	@Override
	public boolean isOverride() {
		return this.propagatedFrom != null;
	}

	@Override
	public ClauseAlias getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public Member propagateTo(Obj owner) {
		return new ClauseAlias(owner, this);
	}

	@Override
	public void resolveAll() {
	}

	@Override
	public final MemberTypeParameter toTypeParameter() {
		return null;
	}

	@Override
	public MemberField toField() {
		return null;
	}

	@Override
	public MemberClause toClause() {
		return getAliasedClause();
	}

	@Override
	public boolean isAlias() {
		return true;
	}

	private MemberKey getAliasedKey() {
		if (this.propagatedFrom != null) {
			return this.propagatedFrom.getAliasedKey();
		}
		return this.aliasedClause.getKnown().getMemberKey();
	}

	private MemberClause getAliasedClause() {
		return this.aliasedClause.get();
	}

	private MemberClause findAliasedClause() {

		final MemberClause aliasedClause =
				getMemberOwner()
				.getContainer()
				.member(this.propagatedFrom.getAliasedKey())
				.toClause();

		assert aliasedClause != null :
			"Can not find an aliased clause";

		return aliasedClause;
	}

}
