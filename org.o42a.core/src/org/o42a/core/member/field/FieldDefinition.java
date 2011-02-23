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
package org.o42a.core.member.field;

import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Placed;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;


public abstract class FieldDefinition extends Placed {

	public static FieldDefinition invalidDefinition(
			LocationInfo location,
			Distributor distributor) {
		return new Invalid(location, distributor);
	}

	public static FieldDefinition ascendantsDefinition(
			AscendantsDefinition ascendants) {
		return new Default(
				ascendants,
				ascendants.distribute(),
				ascendants,
				null);
	}

	public static FieldDefinition fieldDefinition(
			LocationInfo location,
			AscendantsDefinition ascendants,
			BlockBuilder definition) {
		return new Default(
				location,
				ascendants.distribute(),
				ascendants,
				definition != null
				? definition : emptyBlock(location));
	}

	public static FieldDefinition defaultDefinition(
			LocationInfo location,
			Distributor scope) {
		return new Default(
				location,
				scope,
				new AscendantsDefinition(location, scope),
				null);
	}

	public static FieldDefinition arrayDefinition(ArrayInitializer array) {
		return new Array(array);
	}

	public FieldDefinition(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public boolean isValid() {
		return true;
	}

	public final boolean isArray() {
		return getArrayInitializer() != null;
	}

	public abstract ArtifactKind<?> determineArtifactKind();

	public abstract AscendantsDefinition getAscendants();

	public abstract BlockBuilder getDeclarations();

	public abstract ArrayInitializer getArrayInitializer();

	public abstract Ref getValue();

	public abstract FieldDefinition reproduce(Reproducer reproducer);

	protected static ArtifactKind<?> artifactKind(Ref ref) {

		final Resolution resolution = ref.getResolution();

		if (resolution.toArray() != null) {
			return ArtifactKind.ARRAY;
		}

		return ArtifactKind.OBJECT;
	}

	private static final class Invalid extends FieldDefinition {

		public Invalid(LocationInfo location, Distributor distributor) {
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
		public AscendantsDefinition getAscendants() {
			return null;
		}

		@Override
		public BlockBuilder getDeclarations() {
			return null;
		}

		@Override
		public ArrayInitializer getArrayInitializer() {
			return null;
		}

		@Override
		public Ref getValue() {
			return null;
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

	private static final class Array extends FieldDefinition {

		private final ArrayInitializer arrayInitializer;

		Array(ArrayInitializer arrayInitializer) {
			super(arrayInitializer, arrayInitializer.distribute());
			this.arrayInitializer = arrayInitializer;
		}

		@Override
		public ArtifactKind<?> determineArtifactKind() {
			return ArtifactKind.ARRAY;
		}

		@Override
		public AscendantsDefinition getAscendants() {
			return null;
		}

		@Override
		public BlockBuilder getDeclarations() {
			return null;
		}

		@Override
		public ArrayInitializer getArrayInitializer() {
			return this.arrayInitializer;
		}

		@Override
		public Ref getValue() {
			return null;
		}

		@Override
		public FieldDefinition reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			// TODO reproduce array initializer
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public String toString() {
			return this.arrayInitializer.toString();
		}

	}

	private static final class Default extends FieldDefinition {

		private final AscendantsDefinition ascendants;
		private final BlockBuilder declarations;
		private final boolean call;
		private Ref value;

		Default(
				LocationInfo location,
				Distributor scope,
				AscendantsDefinition ascendants,
				BlockBuilder declarations) {
			super(location, scope);
			this.ascendants = ascendants;
			this.call = declarations != null;
			this.declarations =
				declarations != null
				? declarations : emptyBlock(location);
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
		public AscendantsDefinition getAscendants() {
			return this.ascendants;
		}

		@Override
		public BlockBuilder getDeclarations() {
			return this.declarations;
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
					getAscendants(),
					getDeclarations());
		}

		@Override
		public FieldDefinition reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final AscendantsDefinition ascendants =
				this.ascendants.reproduce(reproducer);

			if (ascendants == null) {
				return null;
			}

			return new Default(
					this,
					reproducer.distribute(),
					ascendants,
					this.call ? this.declarations : null);
		}

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();

			out.append(this.ascendants);
			if (this.declarations != null) {
				out.append(this.declarations);
			}

			return out.toString();
		}

	}

}
