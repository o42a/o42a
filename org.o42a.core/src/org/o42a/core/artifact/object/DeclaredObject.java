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

import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.AdapterId;


class DeclaredObject extends PlainObject {

	private final DeclaredObjectField field;

	DeclaredObject(DeclaredObjectField field) {
		super(field);
		this.field = field;
	}

	@Override
	public String toString() {
		return this.field != null ? this.field.toString() : super.toString();
	}

	@Override
	protected Ascendants buildAscendants() {

		Ascendants ascendants = new Ascendants(this);
		final AdapterId adapterId =
			this.field.getDeclaration().getMemberId().getAdapterId();

		if (adapterId != null) {
			ascendants = ascendants.addExplicitSample(
					adapterId.adapterType(getScope().getEnclosingScope()));
		}

		ascendants = this.field.buildAscendants(ascendants);

		if (ascendants.getAncestor() != null) {
			return ascendants;
		}

		return ascendants.setAncestor(voidRef(
						this,
						getScope().getEnclosingScope().distribute())
				.toTypeRef());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		this.field.getMemberRegistry().registerMembers(members);
	}

	@Override
	protected void updateMembers() {
		this.field.updateMembers();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return this.field.define(getScope());
	}

}
