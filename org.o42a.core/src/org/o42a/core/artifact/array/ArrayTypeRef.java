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
package org.o42a.core.artifact.array;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.Scoped;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.log.Loggable;


public class ArrayTypeRef implements ScopeInfo {

	public static ArrayTypeRef arrayTypeRef(
			TypeRef itemTypeRef,
			int dimension) {
		if (itemTypeRef == null) {
			throw new NullPointerException("Item type not specified");
		}
		return new ArrayTypeRef(
				itemTypeRef,
				dimension > 0 ? dimension : 0);
	}

	public static ArrayTypeRef arrayTypeRef(Ref itemTypeRef, int dimension) {
		return new ArrayTypeRef(
				itemTypeRef.toTypeRef(),
				dimension > 0 ? dimension : 0);
	}

	public static ArrayTypeRef arrayTypeObject(Ref itemTypeRef, int dimension) {
		return new ArrayTypeRef(
				itemTypeRef.toStaticTypeRef(),
				dimension > 0 ? dimension : 0);
	}

	private final TypeRef itemTypeRef;
	private final int dimension;

	private ArrayTypeRef(TypeRef itemTypeRef, int dimension) {
		this.itemTypeRef = itemTypeRef;
		this.dimension = dimension;
	}

	@Override
	public final CompilerContext getContext() {
		return this.itemTypeRef.getContext();
	}

	@Override
	public final Scope getScope() {
		return this.itemTypeRef.getScope();
	}

	public final TypeRef getItemTypeRef() {
		return this.itemTypeRef;
	}

	public final int getDimension() {
		return this.dimension;
	}

	public final boolean isValid() {
		return this.dimension > 0;
	}

	@Override
	public Loggable getLoggable() {
		return this.itemTypeRef.getLoggable();
	}

	public final ArrayTypeRef upgradeScope(Scope scope) {

		final TypeRef typeRef = this.itemTypeRef.upgradeScope(scope);

		if (this.itemTypeRef == typeRef) {
			return this;
		}

		return new ArrayTypeRef(typeRef, this.dimension);
	}

	public ArrayTypeRef commonDerivative(ArrayTypeRef other) {
		if (this.dimension != other.dimension) {
			return null;
		}

		final TypeRelation relation =
				this.itemTypeRef.relationTo(other.itemTypeRef);

		return relation.isPreferred() ? this : other;
	}

	public void resolveAll(Resolver resolver) {
		getItemTypeRef().resolveAll(resolver);
	}

	@Override
	public void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {

		final String type = this.itemTypeRef.toString();
		final StringBuilder out;

		if (!isValid()) {
			out = new StringBuilder(type.length() + 3);
			out.append(type).append("[?]");
		} else {
			out = new StringBuilder(type.length() + (this.dimension << 1));
			out.append(type);
			for (int i = this.dimension - 1; i >= 0; --i) {
				out.append('[');
			}
			for (int i = this.dimension - 1; i >= 0; --i) {
				out.append(']');
			}
		}

		return out.toString();
	}

}
