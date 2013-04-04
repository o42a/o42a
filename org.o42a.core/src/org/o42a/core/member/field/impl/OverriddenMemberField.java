/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.member.field.impl;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;


public abstract class OverriddenMemberField<F extends Field>
		extends MemberField {

	private final MemberField propagatedFrom;

	public OverriddenMemberField(Obj owner, MemberField propagatedFrom) {
		super(
				owner.getLocation().setDeclaration(
						propagatedFrom.getLastDefinition()),
				owner,
				propagatedFrom);
		this.propagatedFrom = propagatedFrom;
	}

	@Override
	public final MemberField getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public abstract OverriddenMemberField<F> propagateTo(Obj owner);

	@Override
	protected final F createField() {

		@SuppressWarnings("unchecked")
		final F propagatedFrom =
				(F) getPropagatedFrom().toField().field(dummyUser());

		return propagateField(propagatedFrom);
	}

	protected abstract F propagateField(F propagatedFrom);

}
