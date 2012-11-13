/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
import org.o42a.core.ScopeInfo;
import org.o42a.core.Scoped;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.util.log.Loggable;


public final class TypeParameter implements ScopeInfo {

	private final int index;
	private final TypeRef typeRef;

	TypeParameter(int index, TypeRef typeRef) {
		assert typeRef != null :
			"Type parameter not specified";
		this.index = index;
		this.typeRef = typeRef;
	}

	@Override
	public final Loggable getLoggable() {
		return getTypeRef().getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return getTypeRef().getContext();
	}

	@Override
	public final Scope getScope() {
		return getTypeRef().getScope();
	}

	public final boolean isValid() {
		return getTypeRef().isValid();
	}

	public final Object getKey() {
		return Integer.valueOf(getIndex());
	}

	public final int getIndex() {
		return this.index;
	}

	public final TypeRef getTypeRef() {
		return this.typeRef;
	}

	public final TypeParameter prefixWith(PrefixPath prefix) {

		final TypeRef oldTypeRef = getTypeRef();
		final TypeRef newTypeRef = oldTypeRef.prefixWith(prefix);

		if (oldTypeRef == newTypeRef) {
			return this;
		}

		return new TypeParameter(getIndex(), newTypeRef);
	}

	public final TypeParameter reproduce(Reproducer reproducer) {

		final TypeRef typeRef = getTypeRef().reproduce(reproducer);

		if (typeRef == null) {
			return null;
		}

		return new TypeParameter(getIndex(), typeRef);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		if (this.typeRef == null) {
			return super.toString();
		}
		return this.typeRef.toString();
	}

}
