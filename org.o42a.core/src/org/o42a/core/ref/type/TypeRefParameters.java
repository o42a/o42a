/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.core.ref.type.impl.DefaultTypeRefParameters;
import org.o42a.core.ref.type.impl.ObjectTypeParametersBuilder;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;


public abstract class TypeRefParameters implements ScopeInfo {

	public static TypeRefParameters defaultTypeRefParameters(
			ScopeInfo location) {
		return new DefaultTypeRefParameters(location, location.getScope());
	}

	public static TypeRefParameters defaultTypeRefParameters(
			LocationInfo location,
			Scope scope) {
		return new DefaultTypeRefParameters(location, scope);
	}

	public abstract TypeParameters<?> refine(
			TypeParameters<?> defaultParameters);

	public ObjectTypeParameters toObjectTypeParameters() {
		return new ObjectTypeParametersBuilder(this);
	}

	public abstract TypeRefParameters prefixWith(PrefixPath prefix);

	public TypeRefParameters rescope(Scope toScope) {

		final Scope scope = getScope();

		if (scope.is(toScope)) {
			return this;
		}

		final PrefixPath prefix =
				toScope.pathTo(scope).bind(this).toPrefix(toScope);

		return prefixWith(prefix);
	}

	public abstract TypeRefParameters reproduce(Reproducer reproducer);

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

}
