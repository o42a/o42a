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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDecl;
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

	public abstract TypeRef typeRef(TargetRef ref);

	public abstract FieldDecl<A> fieldDecl(DeclaredField<A> field);

	public abstract FieldIR<A> fieldIR(IRGenerator generator, Field<A> field);

	public final boolean is(ArtifactKind<?> kind) {
		return this == kind;
	}

	private static final class ObjectKind extends ArtifactKind<Obj> {

		@Override
		public FieldDecl<Obj> fieldDecl(DeclaredField<Obj> field) {
			return Obj.fieldDecl(field);
		}

		@Override
		public Obj cast(Artifact<?> artifact) {
			return artifact.toObject();
		}

		@Override
		public TypeRef typeRef(TargetRef ref) {
			return ref.getRef().toTypeRef().rescope(ref.getRescoper());
		}

		@Override
		public FieldIR<Obj> fieldIR(IRGenerator generator, Field<Obj> field) {
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
		public FieldDecl<Link> fieldDecl(DeclaredField<Link> field) {
			return Link.fieldDecl(field, this);
		}

		@Override
		public Link cast(Artifact<?> artifact) {
			return artifact.toLink();
		}

		@Override
		public TypeRef typeRef(TargetRef ref) {

			final Link link = ref.getArtifact().toLink();

			if (link == null) {
				return null;
			}

			return new LinkTypeRef(ref).toTypeRef();
		}

		@Override
		public FieldIR<Link> fieldIR(IRGenerator generator, Field<Link> field) {
			return Link.fieldIR(generator, field);
		}

		@Override
		public String toString() {
			return this.variable ? "VARIABLE" : "LINK";
		}

	}

	private static final class ArrayKind extends ArtifactKind<Array> {

		@Override
		public FieldDecl<Array> fieldDecl(DeclaredField<Array> field) {
			return Array.fieldDecl(field);
		}

		@Override
		public Array cast(Artifact<?> artifact) {
			return artifact.toArray();
		}

		@Override
		public TypeRef typeRef(TargetRef ref) {
			return null;
		}

		@Override
		public FieldIR<Array> fieldIR(
				IRGenerator generator,
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
		public HostOp target(Code code, CodePos exit) {

			final ObjectIR ir =
				getRef().getResolution().toObject().ir(getGenerator());

			return ir.op(getBuilder(), code);
		}

	}

}
