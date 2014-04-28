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
package org.o42a.core;

import org.o42a.core.source.LocationInfo;


public interface ScopeInfo extends LocationInfo {

	Scope getScope();

	default boolean assertScopeIs(Scope scope) {
		// Don't place an assertion here. JDK-8025141
		Scoped.assertScopeIs(this, scope);
		return true;
	}

	default boolean assertCompatible(Scope scope) {
		// Don't place an assertion here. JDK-8025141
		Scoped.assertCompatible(this, scope);
		return true;
	}

	default boolean assertSameScope(ScopeInfo other) {
		// Don't place an assertion here. JDK-8025141
		Scoped.assertSameScope(this, other);
		return true;
	}

	default boolean assertCompatibleScope(ScopeInfo other) {
		// Don't place an assertion here. JDK-8025141
		Scoped.assertCompatibleScope(this, other);
		return true;
	}

}
