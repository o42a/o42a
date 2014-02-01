/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.impl.StaticTypeRelation;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class StaticTypeRef extends TypeRef {

	private final Ref intactRef;

	StaticTypeRef(Ref intactRef, Ref ref, TypeRefParameters parameters) {
		super(ref, parameters);
		this.intactRef = intactRef;
	}

	@Override
	public final boolean isStatic() {
		return true;
	}

	@Override
	public final Ref getIntactRef() {
		return this.intactRef;
	}

	@Override
	public final StaticTypeRef setParameters(TypeRefParameters parameters) {
		return super.setParameters(parameters).toStatic();
	}

	@Override
	public StaticTypeRef setLocation(LocationInfo location) {

		final Ref oldRef = getRef();
		final Ref oldIntactRef = getIntactRef();
		final Ref newRef = oldRef.setLocation(location);
		final Ref newIntactRef;

		if (oldRef != oldIntactRef) {
			newIntactRef = oldIntactRef.setLocation(location);
		} else {
			newIntactRef = newRef;
		}

		return new StaticTypeRef(
				newIntactRef,
				newRef,
				copyParameters());
	}

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
	public final StaticTypeRef reproduce(Reproducer reproducer) {
		return (StaticTypeRef) super.reproduce(reproducer);
	}

	public final TypeRelation relationTo(StaticTypeRef other) {
		return new StaticTypeRelation(this, other, false);
	}

	@Override
	protected TypeRef create(
			Ref intactRef,
			Ref ref,
			TypeRefParameters parameters) {
		return new StaticTypeRef(intactRef, ref, parameters);
	}

}
