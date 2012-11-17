/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.ref.path.Path;


public final class ClauseAlias extends Alias {

	private final MemberId memberId;
	private final ClauseAlias propagatedFrom;
	private MemberClause aliasedClause;
	private MemberKey memberKey;

	public ClauseAlias(MemberId memberId, MemberClause aliasedMember) {
		super(
				aliasedMember,
				aliasedMember.distribute(),
				aliasedMember.getMemberOwner());
		this.memberId = memberId;
		this.propagatedFrom = null;
		this.aliasedClause = aliasedMember;
	}

	private ClauseAlias(MemberOwner owner, ClauseAlias prototype) {
		super(
				addDeclaration(owner, prototype),
				prototype.distributeIn(owner.getContainer()),
				owner);
		this.memberId = prototype.getMemberId();
		this.propagatedFrom = prototype;
		this.memberKey = prototype.getMemberKey();
	}

	@Override
	public final Path getAliased() {
		return getAliasedKey().toPath();
	}

	@Override
	public final MemberId getMemberId() {
		return this.memberId;
	}

	@Override
	public final MemberKey getMemberKey() {
		if (this.memberKey != null) {
			return this.memberKey;
		}
		return this.memberKey = this.memberId.key(getMemberOwner().getScope());
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
	public Member propagateTo(MemberOwner owner) {
		return new ClauseAlias(owner, this);
	}

	@Override
	public void resolveAll() {
	}

	@Override
	public final boolean isTypeParameter() {
		return false;
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
	public MemberLocal toLocal() {
		return null;
	}

	private MemberKey getAliasedKey() {
		if (this.propagatedFrom != null) {
			return this.propagatedFrom.getAliasedKey();
		}
		return this.aliasedClause.getMemberKey();
	}

	private MemberClause getAliasedClause() {
		if (this.aliasedClause != null) {
			return this.aliasedClause;
		}

		this.aliasedClause =
				getMemberOwner()
				.getContainer()
				.member(this.propagatedFrom.getAliasedKey())
				.toClause();

		assert this.aliasedClause != null :
			"Can not find an aliased clause";

		return this.aliasedClause;
	}

}
