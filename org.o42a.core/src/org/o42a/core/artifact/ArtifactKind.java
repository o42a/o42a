/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.impl.decl.DeclaredLinkField;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.link.LinkFieldIR;
import org.o42a.core.ir.field.object.ObjectFieldIR;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.impl.decl.DeclaredObjectField;


public abstract class ArtifactKind<A extends Artifact<A>> {

	public static final ArtifactKind<Obj> OBJECT = new ObjectKind();

	public static final ArtifactKind<Link> LINK = new LinkKind(false);

	public static final ArtifactKind<Link> VARIABLE = new LinkKind(true);

	public boolean isObject() {
		return is(OBJECT);
	}

	public boolean isLink() {
		return !is(OBJECT);
	}

	public boolean isVariable() {
		return is(VARIABLE);
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
			return new DeclaredObjectField(member);
		}

		@Override
		public Obj cast(Artifact<?> artifact) {
			return artifact.toObject();
		}

		@Override
		public FieldIR<Obj> fieldIR(Generator generator, Field<Obj> field) {
			return new ObjectFieldIR(generator, field);
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
			return new DeclaredLinkField(member, this);
		}

		@Override
		public Link cast(Artifact<?> artifact) {
			return artifact.toLink();
		}

		@Override
		public FieldIR<Link> fieldIR(Generator generator, Field<Link> field) {
			return new LinkFieldIR(generator, field);
		}

		@Override
		public String toString() {
			return this.variable ? "VARIABLE" : "LINK";
		}

	}

}
