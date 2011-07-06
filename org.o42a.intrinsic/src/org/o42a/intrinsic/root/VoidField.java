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
package org.o42a.intrinsic.root;

import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectField;
import org.o42a.core.member.MemberOwner;


final class VoidField extends ObjectField {

	VoidField(Root root) {
		super(
				root.toMemberOwner(),
				fieldDeclaration(root, root.distribute(), fieldName("void")));
		setFieldArtifact(getContext().getVoid());
	}

	private VoidField(MemberOwner owner, VoidField sample) {
		super(owner, sample);
	}

	@Override
	public Obj getArtifact() {
		return getFieldArtifact();
	}

	@Override
	public String toString() {
		return "$$void";
	}

	@Override
	protected VoidField propagate(MemberOwner owner) {
		return new VoidField(owner, this);
	}

}
