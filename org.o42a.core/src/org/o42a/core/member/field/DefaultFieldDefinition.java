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
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Call;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;


final class DefaultFieldDefinition extends FieldDefinition {

	private final AscendantsDefinition ascendants;
	private final BlockBuilder definitions;
	private Ref value;

	DefaultFieldDefinition(
			LocationInfo location,
			Distributor scope,
			AscendantsDefinition ascendants,
			BlockBuilder definitions) {
		super(location, scope);
		this.ascendants = ascendants;
		this.definitions = definitions;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		if (this.ascendants.getSamples().length != 0) {
			return ArtifactKind.OBJECT;
		}

		final TypeRef ancestor = this.ascendants.getAncestor();

		if (ancestor == null || !ancestor.validate()) {
			return null;
		}

		return artifactKind(ancestor.getRef());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		this.ascendants.updateAscendants(definer);
		definer.define(this.definitions);
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(getValue(), this.ascendants.getAncestor());
	}

	@Override
	public void defineArray(ArrayDefiner definer) {
		getLogger().error("not_array", this, "Not array");
	}

	@Override
	public FieldDefinition reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final AscendantsDefinition ascendants =
			this.ascendants.reproduce(reproducer);

		if (ascendants == null) {
			return null;
		}

		return new DefaultFieldDefinition(
				this,
				reproducer.distribute(),
				ascendants,
				this.definitions);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(this.ascendants);
		out.append(this.definitions);

		return out.toString();
	}

	private Ref getValue() {
		if (this.value != null) {
			return this.value;
		}

		if (this.ascendants.isEmpty()) {
			getLogger().noDefinition(this);
		}

		return this.value = new Call(
				this,
				distribute(),
				this.ascendants,
				this.definitions);
	}

}
