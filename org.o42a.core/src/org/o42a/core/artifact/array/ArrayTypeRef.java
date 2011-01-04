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

import org.o42a.ast.Node;
import org.o42a.core.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.ref.Ref;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.log.LoggableVisitor;


public class ArrayTypeRef implements ScopeSpec {

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
	public final Node getNode() {
		return this.itemTypeRef.getNode();
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

		final Node node = getNode();

		return node != null ? node : this;
	}

	@Override
	public Object getLoggableData() {
		return this;
	}

	@Override
	public LogInfo getPreviousLogInfo() {
		return null;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {

		final Node node = getNode();

		if (node != null) {
			return node.accept(visitor, p);
		}

		return visitor.visitData(this, p);
	}

	public final ArrayTypeRef toScope(Scope scope) {

		final TypeRef typeRef = this.itemTypeRef.upgradeScope(scope);

		if (this.itemTypeRef == typeRef) {
			return this;
		}

		return new ArrayTypeRef(typeRef, this.dimension);
	}

	public boolean derivedFrom(ArrayTypeRef other) {
		if (this.dimension != other.dimension) {
			return false;
		}
		return this.itemTypeRef.derivedFrom(other.itemTypeRef);
	}

	public ArrayTypeRef commonInheritant(ArrayTypeRef other) {
		if (this.dimension != other.dimension) {
			return null;
		}

		final TypeRef commonInheritant =
			this.itemTypeRef.commonInheritant(other.itemTypeRef);

		if (commonInheritant == null) {
			return null;
		}
		if (commonInheritant == this.itemTypeRef) {
			return this;
		}

		return other;
	}

	@Override
	public void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public void assertSameScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public void assertCompatibleScope(ScopeSpec other) {
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
