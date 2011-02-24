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
package org.o42a.core.ref.type;

import static org.o42a.core.artifact.Artifact.unresolvableObject;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.RescopableRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;


public abstract class TypeRef extends RescopableRef {

	private TypeRef ancestor;
	private Obj type;

	TypeRef(Rescoper rescoper) {
		super(rescoper);
	}

	public abstract Ref getUntachedRef();

	public final TypeRef getAncestor() {
		if (this.ancestor != null) {
			return this.ancestor;
		}
		return this.ancestor =
			getUntachedRef().ancestor(this).rescope(getRescoper());
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

	public boolean validate() {
		return getType() != null;
	}

	public final TypeRelation relationTo(TypeRef other) {
		return relationTo(other, true);
	}

	public TypeRelation relationTo(
			TypeRef other,
			boolean reportIncompatibility) {
		assertSameScope(other);
		if (!other.validate()) {
			return TypeRelation.PREFERRED;
		}
		if (!validate()) {
			return TypeRelation.INVALID;
		}

		final Scope root1 = getRef().getResolutionRoot().resolve(
				this,
				getRescoper().rescope(getScope())).getScope();
		final Scope root2 = other.getRef().getResolutionRoot().resolve(
				other,
				other.getRescoper().rescope(other.getScope())).getScope();

		final Obj type1 = getType();
		final Obj type2 = other.getType();

		if (root1 == root2) {
			if (type1.getScope() == type2.getScope()) {
				return TypeRelation.SAME;
			}
			if (type1.derivedFrom(type2)) {
				return TypeRelation.DERIVATIVE;
			}
			if (type2.derivedFrom(type1)) {
				return TypeRelation.ASCENDANT;
			}
			if (reportIncompatibility) {
				getLogger().incompatible(other, this);
			}
			return TypeRelation.INCOMPATIBLE;
		}

		if (root2.contains(root1)) {
			if (type1.derivedFrom(type2)) {
				return TypeRelation.DERIVATIVE;
			}
			if (reportIncompatibility) {
				getLogger().notDerivedFrom(this, other);
			}
			return TypeRelation.INCOMPATIBLE;
		}

		if (root1.contains(root2)) {
			if (type2.derivedFrom(type1)) {
				return TypeRelation.ASCENDANT;
			}
			if (reportIncompatibility) {
				getLogger().notDerivedFrom(other, this);
			}
			return TypeRelation.INCOMPATIBLE;
		}

		getLogger().incompatible(other, this);

		return TypeRelation.INCOMPATIBLE;
	}

	public abstract StaticTypeRef toStatic();

	public final TypeRef commonDerivative(TypeRef other) {
		return relationTo(other).isPreferred() ? this : other;
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
	protected abstract TypeRef create(
			Rescoper rescoper,
			Rescoper additionalRescoper);

	@Override
	protected final TypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Rescoper rescoper) {

		final Ref untouchedRef;

		if (getRef() == getUntachedRef()) {
			untouchedRef = ref;
		} else {
			untouchedRef = getUntachedRef().reproduce(rescopedReproducer);
			if (untouchedRef == null) {
				return null;
			}
		}

		return createReproduction(
				reproducer,
				rescopedReproducer,
				ref,
				untouchedRef,
				rescoper);
	}

	protected abstract TypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			Rescoper rescoper);

}
