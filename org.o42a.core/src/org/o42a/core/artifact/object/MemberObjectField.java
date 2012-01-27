/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.artifact.object;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.impl.field.OverriddenMemberField;


final class MemberObjectField extends MemberField {

	MemberObjectField(MemberOwner owner, FieldDeclaration declaration) {
		super(owner, declaration);
	}

	@Override
	public final ArtifactKind<Obj> getArtifactKind() {
		return ArtifactKind.OBJECT;
	}

	@Override
	public MemberField getPropagatedFrom() {
		return null;
	}

	@Override
	public MemberField propagateTo(MemberOwner owner) {
		return new Propagated(owner, this);
	}

	@Override
	protected Field<?> createField() {
		throw new UnsupportedOperationException();
	}

	final void init(ObjectField field) {
		setField(dummyUser(), field);
	}

	private static final class Propagated
			extends OverriddenMemberField<ObjectField> {

		Propagated(MemberOwner owner, MemberField propagatedFrom) {
			super(owner, propagatedFrom);
		}

		@Override
		public Propagated propagateTo(MemberOwner owner) {
			return new Propagated(owner, this);
		}

		@Override
		protected ObjectField propagateField(ObjectField propagatedFrom) {
			return propagatedFrom.propagate(this);
		}

	}

}
