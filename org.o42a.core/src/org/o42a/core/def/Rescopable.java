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
package org.o42a.core.def;

import org.o42a.core.*;


public abstract class Rescopable implements ScopeSpec {

	private final Rescoper rescoper;

	public Rescopable(Rescoper rescoper) {
		this.rescoper = rescoper;
	}

	@Override
	public final Scope getScope() {
		return this.rescoper.getFinalScope();
	}

	public final Rescoper getRescoper() {
		return this.rescoper;
	}

	public Rescopable rescope(Rescoper rescoper) {

		final Rescoper oldRescoper = getRescoper();

		if (rescoper.getFinalScope() == oldRescoper.getFinalScope()) {
			return this;
		}

		final Rescoper newRescoper = oldRescoper.and(rescoper);

		if (newRescoper.equals(oldRescoper)) {
			return this;
		}

		return create(newRescoper, rescoper);
	}

	public Rescopable upgradeScope(Scope scope) {
		if (scope == getScope()) {
			return this;
		}
		return rescope(Rescoper.upgradeRescoper(getScope(), scope));
	}

	public Rescopable rescope(Scope scope) {
		if (getScope() == scope) {
			return this;
		}
		return rescope(getScope().rescoperTo(scope));
	}

	public final CompilerLogger getLogger() {
		return getScope().getLogger();
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
	public final void assertSameScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	protected abstract ScopeSpec getScoped();

	protected abstract Rescopable create(
			Rescoper rescoper,
			Rescoper additionalRescoper);

}
