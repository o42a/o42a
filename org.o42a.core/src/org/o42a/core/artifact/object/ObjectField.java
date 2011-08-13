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

import static org.o42a.core.source.CompilerLogger.logDeclaration;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.impl.decl.PropagatedObject;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.OverrideMode;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;


public abstract class ObjectField extends Field<Obj> {

	public ObjectField(MemberOwner owner, FieldDeclaration declaration) {
		super(new MemberObjectField(owner, declaration));
		((MemberObjectField) toMember()).init(this);
	}

	protected ObjectField(MemberOwner owner, ObjectField overridden) {
		super(
				new Location(
						owner.getContext(),
						owner.getLoggable().setReason(
								logDeclaration(
										overridden.getLastDefinition()))),
				owner,
				overridden,
				null,
				OverrideMode.PROPAGATE);
		setFieldArtifact(propagateArtifact(overridden));
	}

	protected ObjectField(
			LocationInfo location,
			MemberOwner owner,
			Field<Obj> overridden,
			Field<Obj> wrapped,
			OverrideMode mode) {
		super(location, owner, overridden, wrapped, mode);
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		final Obj otherObject = other.toObject();

		if (otherObject == null) {
			return false;
		}

		return getArtifact().type().derivedFrom(otherObject.type());
	}

	@Override
	protected Obj propagateArtifact(Field<Obj> overridden) {
		return new PropagatedObject(this);
	}

}
