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

import static org.o42a.core.artifact.object.ConstructionMode.PROHIBITED_CONSTRUCTION;
import static org.o42a.util.use.Usable.simpleUsable;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.ConstructionMode;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.def.RescopableRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.util.Holder;
import org.o42a.util.use.Usable;
import org.o42a.util.use.UserInfo;


public abstract class TypeRef extends RescopableRef<TypeRef> {

	private final Usable<TypeRef> usable = simpleUsable("UsableTypeRef", this);
	private TypeRef ancestor;
	private Holder<ObjectType> type;

	TypeRef(Rescoper rescoper) {
		super(rescoper);
	}

	public abstract boolean isStatic();

	public abstract Ref getUntachedRef();

	public final TypeRef getAncestor() {
		if (this.ancestor != null) {
			return this.ancestor;
		}
		return this.ancestor =
			getUntachedRef().ancestor(this).rescope(getRescoper());
	}

	public ConstructionMode getConstructionMode() {

		final Obj object = typeObject(dummyUser());

		if (object == null) {
			return PROHIBITED_CONSTRUCTION;
		}

		return object.getConstructionMode();
	}

	public ObjectType type(UserInfo user) {
		usable().useBy(user);
		if (this.type != null) {
			return this.type.get();
		}

		final Artifact<?> artifact = artifact(usable());

		if (artifact == null) {
			this.type = new Holder<ObjectType>(null);
			return null;
		}

		final TypeRef typeRef = artifact.getTypeRef();

		if (typeRef != null) {

			final ObjectType type = typeRef.type(usable());

			this.type = new Holder<ObjectType>(type);

			return type;
		}

		final Obj object = artifact.toObject();

		if (object == null) {
			getScope().getLogger().notTypeRef(this);
			this.type = new Holder<ObjectType>(null);
			return null;
		}

		final ObjectType result = object.type().useBy(usable());

		this.type = new Holder<ObjectType>(result);

		return result;
	}

	public final Obj typeObject(UserInfo user) {

		final ObjectType type = type(user);

		return type != null ? type.getObject() : null;
	}

	public boolean validate() {
		return type(dummyUser()) != null;
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
				dummyUser(),
				getRescoper().rescope(getScope())).getScope();
		final Scope root2 = other.getRef().getResolutionRoot().resolve(
				other,
				dummyUser(),
				other.getRescoper().rescope(other.getScope())).getScope();

		final ObjectType type1 = type(dummyUser());
		final ObjectType type2 = other.type(dummyUser());

		if (root1 == root2) {
			if (type1.getObject().getScope() == type2.getObject().getScope()) {
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

	protected final Usable<?> usable() {
		return this.usable;
	}

}
