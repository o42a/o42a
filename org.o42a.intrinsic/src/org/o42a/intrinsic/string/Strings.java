/*
    Intrinsics
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.intrinsic.string;

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.common.object.IntrinsicObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.root.Root;


public class Strings extends IntrinsicObject {

	public Strings(Root owner) {
		super(
				owner.toMemberOwner(),
				fieldDeclaration(
						owner,
						owner.distribute(),
						memberName("strings")));
		setValueType(ValueType.VOID);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				ValueType.VOID.typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		super.declareMembers(members);
		members.addMember(new CompareStrings(this).toMember());
	}

	@Override
	protected Definitions explicitDefinitions() {
		return null;
	}

}
