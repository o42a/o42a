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
package org.o42a.core.artifact.link;

import static org.o42a.core.ref.Ref.falseRef;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.Directive;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;


public abstract class Link extends Artifact<Link> {

	public static DeclaredField<Link, ?> declareField(
			MemberField member,
			ArtifactKind<Link> artifactKind) {
		return new DeclaredLinkField(member, artifactKind);
	}

	public static FieldIR<Link> fieldIR(
			Generator generator,
			Field<Link> field) {
		return new LinkFieldIR(generator, field);
	}

	private final ArtifactKind<Link> kind;
	private Obj target;
	private TargetRef targetRef;

	public Link(Scope scope, ArtifactKind<Link> kind) {
		super(scope);
		this.kind = kind;
	}

	protected Link(Scope scope, Link sample) {
		super(scope, sample);
		this.kind = sample.kind;
	}

	@Override
	public final ArtifactKind<Link> getKind() {
		return this.kind;
	}

	@Override
	public final Obj toObject() {
		return null;
	}

	@Override
	public final Array toArray() {
		return null;
	}

	@Override
	public final Directive toDirective() {
		return materialize().toDirective();
	}

	@Override
	public final Link toLink() {
		return this;
	}

	public final boolean isVariable() {
		return getKind() == ArtifactKind.VARIABLE;
	}

	@Override
	public final TypeRef getTypeRef() {
		return getTargetRef().getTypeRef();
	}

	public final TargetRef getTargetRef() {
		if (this.targetRef == null) {
			define();
		}
		return this.targetRef;
	}

	@Override
	public Obj materialize() {
		if (this.target == null) {
			if (isVariable() || enclosingScopeIsRuntime()) {
				this.target = new RuntimeLinkTarget(this);
			} else {
				this.target = new LinkTarget(this);
			}
		}
		return this.target;
	}

	@Override
	public void resolveAll() {
		getTargetRef().resolveAll();
		materialize().resolveAll();
	}

	protected abstract TargetRef buildTargetRef();

	private void define() {
		this.targetRef = buildTargetRef();
		if (this.targetRef == null) {
			getLogger().error(
					"missing_link_target",
					this,
					"Link target is missing");
			this.targetRef = falseRef(
					this,
					getScope().getEnclosingScope().distribute())
					.toTargetRef(null);
			return;
		}
		this.targetRef.assertSameScope(getScope().getEnclosingScope());

		this.targetRef.assertScopeIs(getScope().getEnclosingScope());
		if (!isAbstract() && !isPrototype()) {
			// FIXME Fix link target access check.
			//this.targetRef.getArtifact().accessBy(this).checkInstanceUse();
		}

		final TypeRef typeRef = this.targetRef.getTypeRef();

		typeRef.getArtifact().accessBy(this).checkPrototypeUse();

		final TypeRelation relation =
			typeRef.relationTo(this.targetRef.toTypeRef());

		if (!relation.isAscendant()) {
			if (!relation.isError()) {
				getLogger().notDerivedFrom(this.targetRef, typeRef);
			}
		}
	}

	private boolean enclosingScopeIsRuntime() {

		final Scope enclosingScope = getScope().getEnclosingScope();

		assert enclosingScope != null :
			"Link " + this + " should be declared inside object";

		return enclosingScope.getConstructionMode().isRuntime();
	}

}
