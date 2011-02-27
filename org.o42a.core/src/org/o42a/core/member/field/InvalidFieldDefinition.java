/*
    Compiler Core
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
package org.o42a.core.member.field;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.st.Reproducer;


final class InvalidFieldDefinition extends FieldDefinition {

	public InvalidFieldDefinition(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		return null;
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
	}

	@Override
	public void defineLink(LinkDefiner definer) {
	}

	@Override
	public void defineArray(ArrayDefiner definer) {
	}

	@Override
	public FieldDefinition reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return null;
	}

	@Override
	public String toString() {
		return "INVALID DEFINITION";
	}

}
