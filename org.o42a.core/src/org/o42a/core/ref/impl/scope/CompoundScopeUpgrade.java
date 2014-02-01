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


final class CompoundScopeUpgrade extends ScopeUpgrade {

	static ScopeUpgrade compoundScopeUpgrade(
			ScopeUpgrade first,
			ScopeUpgrade second) {
		if (second.getClass() == CompoundScopeUpgrade.class) {

			final CompoundScopeUpgrade compound =
					(CompoundScopeUpgrade) second;

			return new CompoundScopeUpgrade(
					first.and(compound.first),
					compound.second);
		}

		return new CompoundScopeUpgrade(first, second);
	}

	private final ScopeUpgrade first;
	private final ScopeUpgrade second;

	private CompoundScopeUpgrade(ScopeUpgrade first, ScopeUpgrade second) {
		super(second.getFinalScope());
		this.first = first;
		this.second = second;
	}

	@Override
	public Scope rescope(Scope scope) {
		return this.first.rescope(this.second.rescope(scope));
	}

	@Override
	public Resolver rescope(Resolver resolver) {
		return this.first.rescope(this.second.rescope(resolver));
	}

	@Override
	public ScopeUpgrade and(ScopeUpgrade other) {
		return this.first.and(this.second.and(other));
	}

	@Override
	public PrefixPath toPrefix() {
		return this.first.toPrefix().and(this.second.toPrefix());
	}

	@Override
	public String toString() {
		if (this.second == null) {
			return super.toString();
		}
		return '(' + this.first.toString() + ", " + this.second + ')';
	}

}
