/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.member.alias;

import static org.o42a.core.ref.RefUsage.BODY_REF_USAGE;
import static org.o42a.core.ref.RefUser.refUser;

import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;


public class MemberAliasField extends MemberField {

	private final Ref ref;
	private final MemberField propagatedFrom;

	public MemberAliasField(
			Obj owner,
			FieldDeclaration declaration,
			Ref ref) {
		super(owner, declaration);
		assert ref.assertSameScope(owner);
		this.ref = ref;
		this.propagatedFrom = null;
	}

	private MemberAliasField(
			Obj owner,
			MemberAliasField propagatedFrom) {
		super(
				owner.getLocation().setDeclaration(propagatedFrom),
				owner,
				propagatedFrom);
		this.ref = propagatedFrom.ref.upgradeScope(owner.getScope());
		this.propagatedFrom = propagatedFrom;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public MemberField getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public MemberAliasField propagateTo(Obj owner) {
		return new MemberAliasField(owner, this);
	}

	@Override
	public void resolveAll() {
		super.resolveAll();
		getRef().resolveAll(getScope().resolver().fullResolver(
				refUser(getAnalysis().fieldAccess()),
				BODY_REF_USAGE));
	}

	@Override
	protected Field createField() {
		return new AliasField(this);
	}

}
