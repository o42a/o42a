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
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;


public abstract class FieldDefinition extends Placed {

	public static FieldDefinition invalidDefinition(
			LocationInfo location,
			Distributor distributor) {
		return new InvalidFieldDefinition(location, distributor);
	}

	public static FieldDefinition ascendantsDefinition(
			AscendantsDefinition ascendants) {
		return new DefaultFieldDefinition(
				ascendants,
				ascendants.distribute(),
				ascendants,
				null);
	}

	public static FieldDefinition fieldDefinition(
			LocationInfo location,
			AscendantsDefinition ascendants,
			BlockBuilder definition) {
		return new DefaultFieldDefinition(
				location,
				ascendants.distribute(),
				ascendants,
				definition != null
				? definition : emptyBlock(location));
	}

	public static FieldDefinition defaultDefinition(
			LocationInfo location,
			Distributor scope) {
		return new DefaultFieldDefinition(
				location,
				scope,
				new AscendantsDefinition(location, scope),
				null);
	}

	public static FieldDefinition arrayDefinition(ArrayInitializer array) {
		return new ArrayFieldDefinition(array);
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

	public abstract void defineObject(ObjectDefiner definer);

	public abstract void defineLink(LinkDefiner definer);

	public abstract AscendantsDefinition getAscendants();

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

	public interface Definer<A extends Artifact<A>> {

		Field<A> getField();

	}

	public interface ObjectDefiner extends Definer<Obj> {

		Ascendants getImplicitAscendants();

		Ascendants getAscendants();

		void setAscendants(Ascendants ascendants);

		void define(BlockBuilder definitions);

	}

	public interface LinkDefiner extends Definer<Link> {

		TypeRef getTypeRef();

		TargetRef getDefaultTargetRef();

		TargetRef getTargetRef();

		void setTargetRef(TargetRef targetRef);

	}

}
