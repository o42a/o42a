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
package org.o42a.core.ref;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ref.impl.scope.DerivedScopeUpgrade;
import org.o42a.core.ref.impl.scope.NoScopeUpgrade;
import org.o42a.core.ref.impl.scope.ScopeWrap;
import org.o42a.core.ref.path.PrefixPath;


public abstract class ScopeUpgrade {

	public static ScopeUpgrade noScopeUpgrade(Scope scope) {
		assert scope != null :
			"Scope not specified";
		return new NoScopeUpgrade(scope);
	}

	public static ScopeUpgrade upgradeScope(ScopeInfo scoped, Scope toScope) {
		scoped.assertCompatible(toScope);
		return new DerivedScopeUpgrade(toScope);
	}

	public static ScopeUpgrade wrapScope(
			Scope wrapperScope,
			Scope wrappedScope) {
		return new ScopeWrap(wrapperScope, wrappedScope);
	}

	private final Scope finalScope;

	public ScopeUpgrade(Scope finalScope) {
		this.finalScope = finalScope;
	}

	public boolean isUpgrade() {
		return true;
	}

	public final Scope getFinalScope() {
		return this.finalScope;
	}

	public final boolean upgradeOf(ScopeInfo scoped) {
		return isUpgrade() && getFinalScope() != scoped.getScope();
	}

	public abstract Scope rescope(Scope scope);

	public abstract Resolver rescope(Resolver resolver);

	public final FullResolver rescope(FullResolver fullResolver) {

		final Resolver oldResolver = fullResolver.getResolver();
		final Resolver newResolver = rescope(oldResolver);

		if (oldResolver == newResolver) {
			return fullResolver;
		}

		return newResolver.fullResolver(fullResolver, fullResolver.refUsage());
	}

	public abstract ScopeUpgrade and(ScopeUpgrade other);

	public abstract PrefixPath toPrefix();

}
