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
package org.o42a.core.ref.impl.scope;

import org.o42a.core.Scope;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ScopeUpgrade;
import org.o42a.core.ref.path.PrefixPath;


public final class NoScopeUpgrade extends ScopeUpgrade {

	public NoScopeUpgrade(Scope finalScope) {
		super(finalScope);
	}

	@Override
	public boolean isUpgrade() {
		return false;
	}

	@Override
	public Scope rescope(Scope scope) {
		return scope;
	}

	@Override
	public Resolver rescope(Resolver resolver) {
		return resolver;
	}

	@Override
	public ScopeUpgrade and(ScopeUpgrade other) {
		return other;
	}

	@Override
	public PrefixPath toPrefix() {
		return PrefixPath.emptyPrefix(getFinalScope());
	}

	@Override
	public String toString() {

		final Scope finalScope = getFinalScope();

		if (finalScope == null) {
			return super.toString();
		}

		return finalScope.toString();
	}

}
