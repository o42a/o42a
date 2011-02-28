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

import static org.o42a.core.def.Definitions.emptyDefinitions;

import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.type.TypeRef;


final class PropagatedObject extends PlainObject {

	public static Ascendants deriveSamples(
			Field<Obj> field,
			Ascendants ascendants) {

		final Obj container = field.getEnclosingContainer().toObject();
		final TypeRef containerAncestor = container.getAncestor();

		if (containerAncestor != null) {

			final Member overridden =
				containerAncestor.getType().member(field.getKey());

			if (overridden != null) {
				ascendants = ascendants.addMemberOverride(overridden);
			}
		}

		final Sample[] containerSamples = container.getSamples();

		for (int i = containerSamples.length - 1; i >= 0; --i) {

			final Member overridden =
				containerSamples[i].getType().member(field.getKey());

			if (overridden != null) {
				ascendants = ascendants.addMemberOverride(overridden);
			}
		}

		return ascendants;
	}

	PropagatedObject(Field<Obj> field) {
		super(field, field.getOverridden()[0].getArtifact());
	}

	@Override
	public boolean isPropagated() {
		return true;
	}

	@Override
	public String toString() {

		final Field<Obj> field = field();

		return field != null ? field.toString() : super.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return deriveSamples(field(), new Ascendants(getScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

	@SuppressWarnings("unchecked")
	private final Field<Obj> field() {
		return (Field<Obj>) getScope();
	}

}
