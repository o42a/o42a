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
package org.o42a.core.artifact.object;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;


public abstract class ObjectField extends Field<Obj> {

	public ObjectField(FieldDeclaration declaration) {
		super(new Member(declaration));
		((Member) toMember()).field = this;
	}

	public ObjectField(Container enclosingContainer, String name) {
		super(new Member(enclosingContainer, name));
		((Member) toMember()).field = this;
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		final Obj otherObject = other.getContainer().toObject();

		if (otherObject == null) {
			return false;
		}

		return getArtifact().derivedFrom(otherObject);
	}

	protected ObjectField(
			Container enclosingContainer,
			ObjectField overridden) {
		super(enclosingContainer, overridden);
	}

	protected ObjectField(
			Container enclosingContainer,
			ObjectField overridden,
			boolean propagate) {
		super(enclosingContainer, overridden, propagate);
	}

	@Override
	protected Obj propagateArtifact(Field<Obj> overridden) {
		return new PropagatedObject(this);
	}

	private static final class Member extends MemberField {

		private ObjectField field;

		public Member(Container container, String name) {
			super(container, name);
		}

		public Member(FieldDeclaration declaration) {
			super(declaration);
		}

		@Override
		public Field<?> toField() {
			return this.field;
		}

	}

}
