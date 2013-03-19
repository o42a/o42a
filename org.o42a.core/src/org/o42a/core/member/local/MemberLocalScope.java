/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Distributor;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.impl.PropagatedMemberLocalScope;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.object.OwningObject;
import org.o42a.core.source.LocationInfo;


public abstract class MemberLocalScope extends Member {

	public MemberLocalScope(
			LocationInfo location,
			Distributor distributor,
			OwningObject owner) {
		super(location, distributor, owner);
	}

	protected MemberLocalScope(
			MemberOwner owner,
			MemberLocalScope propagatedFrom) {
		super(
				owner.getLocation().setDeclaration(
						propagatedFrom.getLastDefinition()),
				propagatedFrom.distributeIn(owner.getContainer()),
				owner);
	}

	public final Obj getOwner() {
		return getOwningObject().getObject();
	}

	public final OwningObject getOwningObject() {
		return (OwningObject) getMemberOwner();
	}

	@Override
	public final MemberTypeParameter toTypeParameter() {
		return null;
	}

	@Override
	public final MemberField toField() {
		return null;
	}

	@Override
	public final MemberClause toClause() {
		return null;
	}

	@Override
	public final MemberLocalScope toLocalScope() {
		return this;
	}

	@Override
	public final Alias toAlias() {
		return null;
	}

	public abstract LocalScope localScope();

	@Override
	public final LocalScope substance(UserInfo user) {
		return localScope();
	}

	@Override
	public final Visibility getVisibility() {
		if (toClause() != null) {
			return Visibility.PUBLIC;
		}
		return Visibility.PRIVATE;
	}

	@Override
	public final boolean isOverride() {
		return isPropagated();
	}

	@Override
	public final MemberLocalScope propagateTo(MemberOwner owner) {

		final Obj ownerObject = owner.getContainer().toObject();

		assert ownerObject != null :
			ownerObject + " is not object";

		return new PropagatedMemberLocalScope(owner, this);
	}

	@Override
	public void resolveAll() {
		localScope().resolveAll();
	}

}