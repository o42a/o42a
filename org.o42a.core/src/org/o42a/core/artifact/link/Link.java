/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.*;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDecl;


public abstract class Link extends Artifact<Link> {

	public static FieldDecl<Link> fieldDecl(
			DeclaredField<Link> field,
			ArtifactKind<Link> kind) {
		return new LinkFieldDecl(field, kind);
	}

	public static FieldIR<Link> fieldIR(
			IRGenerator generator,
			Field<Link> field) {
		return new LinkFieldIR(generator, field);
	}

	private final ArtifactKind<Link> kind;
	private Obj target;
	private TargetRef targetRef;
	private TypeRef typeRef;

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

	public final Obj getType() {
		return getTypeRef().getType();
	}

	@Override
	public final TypeRef getTypeRef() {
		if (this.typeRef == null) {
			define();
		}
		return this.typeRef;
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
		getType();
		materialize().resolveAll();
	}

	protected abstract TypeRef buildTypeRef();

	protected abstract TargetRef buildTargetRef();

	protected TypeRef correctTypeRef(TargetRef targetRef, TypeRef typeRef) {
		return refType(targetRef);
	}

	protected TargetRef correctTargetRef(TargetRef targetRef, TypeRef typeRef) {
		return targetRef;
	}

	private void define() {

		final TargetRef ref = buildTargetRef();
		final TypeRef typeRef = buildTypeRef();

		ref.assertCompatible(getScope().getEnclosingScope());
		if (typeRef == null) {
			this.targetRef = ref;
			this.typeRef = ref.getTypeRef();
			return;
		}

		if (!refType(ref).derivedFrom(typeRef)) {
			getLogger().notDerivedFrom(ref, typeRef);
			this.targetRef = correctTargetRef(ref, typeRef);
			this.typeRef = correctTypeRef(ref, typeRef);
			return;
		}

		this.targetRef = ref;
		this.typeRef = typeRef;
	}

	private TypeRef refType(final TargetRef ref) {
		return ref.getTypeRef().upgradeScope(
				getScope().getEnclosingScope());
	}

	private boolean enclosingScopeIsRuntime() {

		final Container enclosingContainer = getScope().getEnclosingContainer();

		return enclosingContainer != null
		&& enclosingContainer.getScope().isRuntime();
	}

}
