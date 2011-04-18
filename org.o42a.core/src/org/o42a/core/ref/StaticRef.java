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
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.artifact.array.ArrayTypeRef;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.value.Value;


final class StaticRef extends Ref {

	private final Ref ref;

	StaticRef(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	@Override
	public Value<?> value(Scope scope) {
		return calculateValue(getResolution().materialize(), scope);
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

		return new StaticRef(ref);
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

		return artifact.selfRef().op(host);
	}

	private static final class FixedFieldDefinition extends FieldDefinition {

		private final FieldDefinition definition;

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
		public void defineArray(ArrayDefiner definer) {
			this.definition.defineArray(new ArrayDefinerWrap(definer));
		}

		@Override
		public void defineLink(LinkDefiner definer) {
			this.definition.defineLink(new LinkDefinerWrap(definer));
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

		ObjectDefinerWrap(ObjectDefiner definer) {
			this.definer = definer;
		}

		@Override
		public Field<Obj> getField() {
			return this.definer.getField();
		}

		@Override
		public Ascendants getImplicitAscendants() {
			return this.definer.getImplicitAscendants();
		}

		@Override
		public ObjectDefiner setAncestor(TypeRef explicitAncestor) {
			this.definer.setAncestor(explicitAncestor.toStatic());
			return this;
		}

		@Override
		public ObjectDefiner addExplicitSample(
				StaticTypeRef explicitAscendant) {
			this.definer.addExplicitSample(explicitAscendant);
			return this;
		}

		@Override
		public ObjectDefiner addImplicitSample(
				StaticTypeRef implicitAscendant) {
			this.definer.addImplicitSample(implicitAscendant);
			return this;
		}

		@Override
		public ObjectDefiner addMemberOverride(Member overriddenMember) {
			this.definer.addMemberOverride(overriddenMember);
			return this;
		}

		@Override
		public void define(BlockBuilder definitions) {
			this.definer.define(definitions);
		}

		@Override
		public String toString() {
			if (this.definer == null) {
				return super.toString();
			}
			return this.definer.toString();
		}

	}

	private static final class LinkDefinerWrap implements LinkDefiner {

		private final LinkDefiner definer;

		LinkDefinerWrap(LinkDefiner definer) {
			this.definer = definer;
		}

		@Override
		public Field<Link> getField() {
			return this.definer.getField();
		}

		@Override
		public TypeRef getTypeRef() {
			return this.definer.getTypeRef();
		}

		@Override
		public TargetRef getDefaultTargetRef() {
			return this.definer.getDefaultTargetRef();
		}

		@Override
		public TargetRef getTargetRef() {
			return this.definer.getTargetRef();
		}

		@Override
		public void setTargetRef(Ref targetRef, TypeRef defaultType) {
			this.definer.setTargetRef(
					targetRef.toStatic(),
					defaultType.toStatic());
		}

	}

	private static final class ArrayDefinerWrap implements ArrayDefiner {

		private final ArrayDefiner definer;

		ArrayDefinerWrap(ArrayDefiner definer) {
			this.definer = definer;
		}

		@Override
		public Field<Array> getField() {
			return this.definer.getField();
		}

		@Override
		public ArrayTypeRef getTypeRef() {
			return this.definer.getTypeRef();
		}

		@Override
		public TypeRef getItemTypeRef() {
			return this.definer.getItemTypeRef();
		}

		@Override
		public void define(ArrayInitializer initializer) {
			this.definer.define(initializer.toStatic());
		}

	}
}
