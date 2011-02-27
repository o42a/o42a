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
package org.o42a.core.ref;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.member.field.*;
import org.o42a.core.st.Reproducer;


final class ValueFieldDefinition extends FieldDefinition {

	private final Ref value;

	ValueFieldDefinition(Ref value) {
		super(value, value.distribute());
		this.value = value;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		return artifactKind(this.value);
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAscendants(
				definer.getAscendants().addImplicitSample(
						this.value.toStaticTypeRef()));
		//definer.define(valueBlock(this.value));
	}

	@Override
	public void defineArray(ArrayDefiner definer) {
		definer.define(ArrayInitializer.valueArrayInitializer(this.value));
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(this.value.toTargetRef(definer.getTypeRef()));
	}

	@Override
	public FieldDefinition reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref value = this.value.reproduce(reproducer);

		if (value == null) {
			return null;
		}

		return new ValueFieldDefinition(value);
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

}
