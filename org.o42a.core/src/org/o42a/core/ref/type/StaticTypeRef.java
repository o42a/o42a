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
package org.o42a.core.ref.type;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.ObjectType;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStructFinder;


public abstract class StaticTypeRef extends TypeRef {

	public StaticTypeRef(PrefixPath prefix) {
		super(prefix);
	}

	@Override
	public final boolean isStatic() {
		return true;
	}

	@Override
	public abstract StaticTypeRef setValueStruct(
			ValueStructFinder valueStructFinder);

	@Override
	public final StaticTypeRef toStatic() {
		return this;
	}

	@Override
	public final StaticTypeRef prefixWith(PrefixPath prefix) {
		return (StaticTypeRef) super.prefixWith(prefix);
	}

	@Override
	public final StaticTypeRef upgradeScope(Scope toScope) {
		return (StaticTypeRef) super.upgradeScope(toScope);
	}

	@Override
	public final StaticTypeRef rescope(Scope scope) {
		return (StaticTypeRef) super.rescope(scope);
	}

	@Override
	public StaticTypeRef reproduce(Reproducer reproducer) {
		return (StaticTypeRef) super.reproduce(reproducer);
	}

	public final TypeRelation relationTo(StaticTypeRef other) {
		return relationTo(other, true);
	}

	public TypeRelation relationTo(
			StaticTypeRef other,
			boolean reportIncompatibility) {
		assertSameScope(other);
		if (!other.validate()) {
			return TypeRelation.PREFERRED;
		}
		if (!validate()) {
			return TypeRelation.INVALID;
		}

		final ObjectType type1 =
				type(dummyUser()).getLastDefinition().type();
		final ObjectType type2 =
				other.type(dummyUser()).getLastDefinition().type();

		if (type1.getObject().getScope() == type2.getObject().getScope()) {
			return TypeRelation.SAME;
		}
		if (type2.derivedFrom(type1)) {
			return TypeRelation.ASCENDANT;
		}
		if (type1.derivedFrom(type2)) {
			return TypeRelation.DERIVATIVE;
		}
		if (reportIncompatibility) {
			getLogger().incompatible(other, this);
		}

		return TypeRelation.INCOMPATIBLE;
	}

	@Override
	protected abstract StaticTypeRef create(
			PrefixPath prefix,
			PrefixPath additionalPrefix);

	@Override
	protected abstract StaticTypeRef createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Ref ref,
			Ref untouchedRef,
			PrefixPath prefix);

}
