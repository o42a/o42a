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

import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;


final class DefaultFieldDefinition extends FieldDefinition {

	private final AscendantsDefinition ascendants;
	private final BlockBuilder definitions;
	private final boolean call;
	private Ref value;

	DefaultFieldDefinition(
			LocationInfo location,
			Distributor scope,
			AscendantsDefinition ascendants,
			BlockBuilder definitions) {
		super(location, scope);
		this.ascendants = ascendants;
		this.call = definitions != null;
		this.definitions =
			definitions != null
			? definitions : emptyBlock(location);
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
		definer.setAscendants(
				this.ascendants.updateAscendants(definer.getAscendants()));
		definer.define(this.definitions);
	}

	@Override
	public AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	@Override
	public ArrayInitializer getArrayInitializer() {
		return null;
	}

	@Override
	public Ref getValue() {
		if (this.value != null) {
			return this.value;
		}

		if (!this.call) {

			final StaticTypeRef[] samples = getAscendants().getSamples();

			if (samples.length == 0) {

				final TypeRef ancestor = getAscendants().getAncestor();

				if (ancestor != null) {
					// ancestor and no samples
					return this.value = ancestor.getRef();
				}

				// implied scope expression

				return null;
			}
			if (samples.length == 1) {
				// single sample
				return this.value = samples[0].getRef();
			}
		}

		if (getAscendants().isEmpty()) {
			getLogger().noDefinition(this);
		}

		return this.value = new DefinitionValue(
				this,
				distribute(),
				this.ascendants,
				this.definitions);
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
				this.call ? this.definitions : null);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(this.ascendants);
		if (this.definitions != null) {
			out.append(this.definitions);
		}

		return out.toString();
	}

}
