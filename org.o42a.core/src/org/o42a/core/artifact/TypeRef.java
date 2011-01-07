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

import static org.o42a.core.artifact.Artifact.unresolvableObject;
import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.ScopeSpec;
import org.o42a.core.Scoped;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.RescopableRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;


public class TypeRef extends RescopableRef {

	public static TypeRef typeRef(Ref ref) {
		if (ref == null) {
			throw new NullPointerException("Type reference not specified");
		}
		return new TypeRef(ref, transparentRescoper(ref.getScope()));
	}

	public static StaticTypeRef staticTypeRef(Ref ref) {
		if (ref == null) {
			throw new NullPointerException("Type reference not specified");
		}
		return new StaticTypeRef(ref, transparentRescoper(ref.getScope()));
	}

	private Obj type;

	TypeRef(Ref ref, Rescoper rescoper) {
		super(ref, rescoper);
	}

	public boolean isStatic() {
		return false;
	}

	public Obj getType() {

		final Obj type = this.type;

		if (type != null) {
			if (type != unresolvableObject(getContext())) {
				return type;
			}
			return null;
		}

		final Artifact<?> artifact = getArtifact();

		if (artifact == null) {
			this.type = unresolvableObject(getContext());
			return null;
		}

		final TypeRef typeRef = artifact.getTypeRef();

		if (typeRef != null) {
			return this.type = typeRef.getType();
		}

		final Obj object = artifact.toObject();

		if (object == null) {
			getScope().getLogger().notTypeRef(this);
			this.type = unresolvableObject(getContext());
			return null;
		}

		return this.type = object;
	}

	public boolean derivedFrom(TypeRef ascendant) {

		final Obj type1 = getType();

		if (type1 == null) {
			return false;
		}

		final Obj type2 = ascendant.getType();

		if (type2 == null) {
			return false;
		}

		return type1.derivedFrom(type2);
	}

	public boolean validate() {
		return getType() != null;
	}

	public StaticTypeRef toStatic() {
		return new StaticTypeRef(getRef(), getRescoper());
	}

	public TypeRef commonInheritant(TypeRef other) {
		if (derivedFrom(other)) {
			return this;
		}
		if (other.derivedFrom(this)) {
			return other;
		}
		return null;
	}

	@Override
	public TypeRef rescope(Rescoper rescoper) {
		return (TypeRef) super.rescope(rescoper);
	}

	@Override
	public TypeRef rescope(Scope scope) {
		return (TypeRef) super.rescope(scope);
	}

	@Override
	public TypeRef upgradeScope(Scope scope) {
		return (TypeRef) super.upgradeScope(scope);
	}

	@Override
	public TypeRef reproduce(Reproducer reproducer) {
		return (TypeRef) super.reproduce(reproducer);
	}

	@Override
	public RefOp op(Code code, CodePos exit, HostOp host) {

		final HostOp rescoped = getRescoper().rescope(code, exit, host);

		return getRef().op(rescoped);
	}

	@Override
	public void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public void assertSameScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public void assertCompatibleScope(ScopeSpec other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	protected TypeRef create(Rescoper rescoper, Rescoper additionalRescoper) {
		return new TypeRef(getRef(), rescoper);
	}

	@Override
	protected TypeRef createReproduction(
			Reproducer reproducer,
			Ref reproducedRef,
			Rescoper rescoper) {
		return new TypeRef(reproducedRef, rescoper);
	}

}
