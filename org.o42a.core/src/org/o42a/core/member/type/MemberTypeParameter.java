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
package org.o42a.core.member.type;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.member.NonAliasMember;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.member.type.impl.OverriddenMemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameter;


public abstract class MemberTypeParameter extends NonAliasMember {

	public MemberTypeParameter(
			LocationInfo location,
			Distributor distributor,
			Obj owner) {
		super(location, distributor, owner);
	}

	public final TypeParameter getTypeParameter() {

		final TypeParameter parameter =
				getMemberOwner()
				.toObject()
				.type()
				.getParameters()
				.parameter(getMemberKey());

		assert parameter != null :
			"Type paramter " + getMemberKey()
			+ " not found in " + getMemberOwner();

		return parameter;
	}

	@Override
	public final MemberTypeParameter toTypeParameter() {
		return this;
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
	public final MemberLocal toLocal() {
		return null;
	}

	@Override
	public final Container substance(UserInfo user) {
		return getMemberOwner().toObject();
	}

	@Override
	public final Visibility getVisibility() {
		return Visibility.PUBLIC;
	}

	@Override
	public abstract MemberTypeParameter getPropagatedFrom();

	@Override
	public final MemberTypeParameter propagateTo(Obj owner) {
		return new OverriddenMemberTypeParameter(owner.toObject(), this);
	}

	@Override
	public void resolveAll() {
	}

}
