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

import static org.o42a.core.member.MemberId.clauseName;
import static org.o42a.core.source.CompilerLogger.logDeclaration;

import org.o42a.core.Container;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.source.Location;
import org.o42a.util.use.UserInfo;


public abstract class MemberClause extends Member {

	private final ClauseDeclaration declaration;
	private MemberKey key;
	private MemberId[] aliasIds;
	private MemberKey[] aliasKeys;

	public MemberClause(MemberOwner owner, ClauseDeclaration declaration) {
		super(declaration, declaration.distribute(), owner);
		this.declaration = declaration;
	}

	protected MemberClause(MemberOwner owner, MemberClause propagatedFrom) {
		super(
				new Location(
						owner.getContext(),
						owner.getLoggable().setReason(logDeclaration(
								propagatedFrom.getLastDefinition()))),
				propagatedFrom.distributeIn(owner.getContainer()),
				owner);
		this.key = propagatedFrom.getKey();
		this.declaration = propagatedFrom.declaration.overrideBy(this);
	}

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public final MemberId getId() {
		return this.declaration.getMemberId();
	}

	@Override
	public final MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = getDeclaration().getMemberId().key(getScope());
	}

	@Override
	public MemberId[] getAliasIds() {
		if (this.aliasIds != null) {
			return this.aliasIds;
		}

		final String name = getDeclaration().getName();

		if (name == null) {
			return this.aliasIds = super.getAliasIds();
		}

		final MemberId id = getId();
		final String memberName = id.getName();

		if (memberName == null) {
			return this.aliasIds = super.getAliasIds();
		}

		final MemberId aliasName = clauseName(name);

		if (memberName.equals(aliasName.getName())) {
			return this.aliasIds = super.getAliasIds();
		}

		final MemberId aliasId;
		final MemberId enclosingId = id.getEnclosingId();

		if (enclosingId != null) {
			aliasId = enclosingId.append(aliasName);
		} else {
			aliasId = aliasName;
		}

		return this.aliasIds = new MemberId[] {aliasId};
	}

	@Override
	public MemberKey[] getAliasKeys() {
		if (this.aliasKeys != null) {
			return this.aliasKeys;
		}

		final MemberId[] aliasIds = getAliasIds();

		if (aliasIds.length == 0) {
			return this.aliasKeys = super.getAliasKeys();
		}

		final MemberKey[] aliasKeys = new MemberKey[aliasIds.length];

		for (int i = 0; i < aliasIds.length; ++i) {
			aliasKeys[i] = aliasIds[i].key(getScope());
		}

		return this.aliasKeys = aliasKeys;
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
	public abstract MemberClause propagateTo(MemberOwner owner);

	@Override
	public void resolveAll() {
		toClause().resolveAll();
	}

	@Override
	protected void merge(Member member) {
		throw new IllegalStateException();
	}

}
