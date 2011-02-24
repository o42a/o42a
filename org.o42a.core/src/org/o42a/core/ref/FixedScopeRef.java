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
package org.o42a.core.ref;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.StaticAscendants;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.*;
import org.o42a.core.member.field.FieldDefinition.ObjectDefiner;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;


final class FixedScopeRef extends Ref {

	private final Ref ref;

	FixedScopeRef(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	@Override
	public Resolution resolve(Scope scope) {
		return this.ref.getResolution();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new FixedScopeRef(ref);
	}

	@Override
	public FieldDefinition toFieldDefinition() {
		return new FixedFieldDefinition(this.ref.toFieldDefinition());
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return "&" + this.ref;
	}

	@Override
	protected boolean isKnownStatic() {
		return true;
	}

	@Override
	protected RefOp createOp(HostOp host) {

		final Artifact<?> artifact = this.ref.getResolution().toArtifact();

		assert artifact != null :
			"Not artifact: " + this.ref;

		return artifact.fixedRef(distribute()).op(host);
	}

	private static final class FixedFieldDefinition extends FieldDefinition {

		private final FieldDefinition definition;
		private AscendantsDefinition ascendants;
		private Ref value;

		FixedFieldDefinition(FieldDefinition definition) {
			super(definition, definition.distribute());
			this.definition = definition;
		}

		@Override
		public boolean isValid() {
			return this.definition.isValid();
		}

		@Override
		public ArtifactKind<?> determineArtifactKind() {
			return this.definition.determineArtifactKind();
		}

		@Override
		public void defineObject(ObjectDefiner definer) {
			this.definition.defineObject(new ObjectDefinerWrap(definer));
		}

		@Override
		public AscendantsDefinition getAscendants() {
			if (this.ascendants != null) {
				return this.ascendants;
			}
			return this.ascendants =
				this.definition.getAscendants().toStatic();
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
			return this.value = this.definition.getValue().fixScope();
		}

		@Override
		public FieldDefinition reproduce(Reproducer reproducer) {

			final FieldDefinition definition =
				this.definition.reproduce(reproducer);

			if (definition == null) {
				return null;
			}

			return new FixedFieldDefinition(definition);
		}

	}

	private static final class ObjectDefinerWrap implements ObjectDefiner {

		private final ObjectDefiner definer;
		private StaticAscendants ascendants;

		ObjectDefinerWrap(ObjectDefiner definer) {
			this.definer = definer;
			this.ascendants =
				new StaticAscendants(definer.getImplicitAscendants());
		}

		@Override
		public Field<Obj> getField() {
			return this.definer.getField();
		}

		@Override
		public Ascendants getImplicitAscendants() {
			return this.ascendants;
		}

		@Override
		public void setAscendants(Ascendants ascendants) {
			this.definer.setAscendants(ascendants);
		}

		@Override
		public void setDefinitions(BlockBuilder definitions) {
			this.definer.setDefinitions(definitions);
		}

		@Override
		public String toString() {
			if (this.definer == null) {
				return super.toString();
			}
			return this.definer.toString();
		}

	}

}
