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
package org.o42a.core.artifact;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


public abstract class ArtifactKind<A extends Artifact<A>> {

	public static final ArtifactKind<Obj> OBJECT = new ObjectKind();

	public static final ArtifactKind<Array> ARRAY = new ArrayKind();

	public static final ArtifactKind<Link> LINK = new LinkKind(false);

	public static final ArtifactKind<Link> VARIABLE = new LinkKind(true);

	public boolean isObject() {
		return is(OBJECT) || is(LINK);
	}

	public boolean isVariable() {
		return is(VARIABLE);
	}

	public boolean isArray() {
		return is(ARRAY);
	}

	public boolean isInheritable() {
		return !isArray();
	}

	public abstract A cast(Artifact<?> artifact);

	@SuppressWarnings("unchecked")
	public final Field<A> cast(Field<?> field) {
		assert field.getArtifact().getKind() == this :
			field + " is not " + this;
		return (Field<A>) field;
	}

	public abstract DeclaredField<A, ?> declareField(MemberField member);

	public abstract FieldIR<A> fieldIR(Generator generator, Field<A> field);

	public final boolean is(ArtifactKind<?> kind) {
		return this == kind;
	}

	private static final class ObjectKind extends ArtifactKind<Obj> {

		@Override
		public DeclaredField<Obj, ?> declareField(MemberField member) {
			return Obj.declareField(member);
		}

		@Override
		public Obj cast(Artifact<?> artifact) {
			return artifact.toObject();
		}

		@Override
		public FieldIR<Obj> fieldIR(Generator generator, Field<Obj> field) {
			return Obj.fieldIR(generator, field);
		}

		@Override
		public String toString() {
			return "OBJECT";
		}

	}

	private final static class LinkKind extends ArtifactKind<Link> {

		private final boolean variable;

		LinkKind(boolean variable) {
			this.variable = variable;
		}

		@Override
		public DeclaredField<Link, ?> declareField(MemberField member) {
			return Link.declareField(member, this);
		}

		@Override
		public Link cast(Artifact<?> artifact) {
			return artifact.toLink();
		}

		@Override
		public FieldIR<Link> fieldIR(Generator generator, Field<Link> field) {
			return Link.fieldIR(generator, field);
		}

		@Override
		public String toString() {
			return this.variable ? "VARIABLE" : "LINK";
		}

	}

	private static final class ArrayKind extends ArtifactKind<Array> {

		@Override
		public DeclaredField<Array, ?> declareField(MemberField member) {
			return Array.declareField(member);
		}

		@Override
		public Array cast(Artifact<?> artifact) {
			return artifact.toArray();
		}

		@Override
		public FieldIR<Array> fieldIR(
				Generator generator,
				Field<Array> field) {
			return Array.fieldIR(generator, field);
		}

		@Override
		public String toString() {
			return "ARRAY";
		}

	}

	private static final class LinkTypeRef extends Ref {

		private final TargetRef ref;

		LinkTypeRef(TargetRef ref) {
			super(ref, ref.getScope().distribute());
			this.ref = ref;
		}

		@Override
		public Resolution resolve(Scope scope) {

			final Resolution resolution = this.ref.resolve(scope);

			if (resolution.isError()) {
				return resolution;
			}

			return objectResolution(
					resolution.toArtifact().getTypeRef().getType());
		}

		@Override
		public Value<?> value(Scope scope) {

			final Resolution resolution = this.ref.resolve(scope);

			if (resolution.isError()) {
				return Value.falseValue();
			}

			return resolution.toArtifact().getTypeRef().getValue();
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {

			final TargetRef ref = this.ref.reproduce(reproducer);

			if (ref == null) {
				return null;
			}

			return new LinkTypeRef(ref);
		}

		@Override
		protected RefOp createOp(HostOp host) {
			return new LinkTypeOp(host, this);
		}

	}

	private static final class LinkTypeOp extends RefOp {

		LinkTypeOp(HostOp host, LinkTypeRef ref) {
			super(host, ref);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			final ObjectIR ir =
				getRef().getResolution().toObject().ir(getGenerator());

			return ir.op(getBuilder(), dirs.code());
		}

	}

}
