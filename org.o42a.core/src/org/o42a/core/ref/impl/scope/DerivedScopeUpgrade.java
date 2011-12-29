/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.ref.impl.scope.CompoundScopeUpgrade.compoundScopeUpgrade;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.core.Scope;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ScopeUpgrade;
import org.o42a.core.ref.path.PrefixPath;


public class DerivedScopeUpgrade extends ScopeUpgrade {

	public DerivedScopeUpgrade(Scope finalScope) {
		super(finalScope);
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
		if (!other.isUpgrade()) {
			if (other.getFinalScope() == getFinalScope()) {
				return this;
			}
			return upgradeScope(getFinalScope(), other.getFinalScope());
		}
		if (other.getClass() == DerivedScopeUpgrade.class) {
			return upgradeScope(getFinalScope(), other.getFinalScope());
		}
		return compoundScopeUpgrade(this, other);
	}

	@Override
	public PrefixPath toPrefix() {
		return SELF_PATH.toPrefix(getFinalScope());
	}

	@Override
	public String toString() {

		final Scope finalScope = getFinalScope();

		if (finalScope == null) {
			return super.toString();
		}

		return "UpgradeTo[" + finalScope + ']';
	}

}
