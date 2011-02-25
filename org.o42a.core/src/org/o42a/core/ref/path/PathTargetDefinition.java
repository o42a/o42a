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
package org.o42a.core.ref.path;

import static org.o42a.core.artifact.array.ArrayInitializer.valueArrayInitializer;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;


final class PathTargetDefinition extends FieldDefinition {

	private final AscendantsDefinition ascendants;
	private final Ref target;

	PathTargetDefinition(Ref target) {
		super(target, target.distribute());
		this.ascendants = new AscendantsDefinition(
				target,
				distribute(),
				target.toTypeRef());
		this.target = target;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		return artifactKind(this.target);
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		// TODO Use ancestor of path target as object's ancestor.
		definer.setAscendants(
				definer.getAscendants().setAncestor(this.target.toTypeRef()));
	}

	@Override
	public void defineLink(LinkDefiner definer) {

		final TypeRef typeRef = definer.getTypeRef();

		definer.setTargetRef(this.target.toTargetRef(
				typeRef != null ? typeRef : this.target.toTypeRef()));
	}

	@Override
	public void defineArray(ArrayDefiner definer) {
		definer.define(valueArrayInitializer(this.target));
	}

	@Override
	public AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	@Override
	public FieldDefinition reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref value = this.target.reproduce(reproducer);

		if (value == null) {
			return null;
		}

		return new PathTargetDefinition(value);
	}

	@Override
	public String toString() {
		if (this.target == null) {
			return super.toString();
		}
		return "FieldDefinition[" + this.target.toString() + ']';
	}

}
