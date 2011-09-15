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
package org.o42a.core.ref.impl;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.LinkDefiner;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.ref.Ref;


public final class ValueFieldDefinition extends FieldDefinition {

	private final Ref value;

	public ValueFieldDefinition(Ref value) {
		super(value, value.distribute());
		this.value = value;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		return artifactKind(this.value);
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.addImplicitSample(this.value.toStaticTypeRef());
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(this.value.materialize(), null);
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

}
