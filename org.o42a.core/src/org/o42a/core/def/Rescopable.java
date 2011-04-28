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


public abstract class Rescopable<R extends Rescopable<R>>
		implements ScopeInfo {

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

	public R rescope(Rescoper rescoper) {

		final Rescoper oldRescoper = getRescoper();

		if (rescoper.getFinalScope() == oldRescoper.getFinalScope()) {
			return self();
		}

		final Rescoper newRescoper = oldRescoper.and(rescoper);

		if (newRescoper.equals(oldRescoper)) {
			return self();
		}

		return create(newRescoper, rescoper);
	}

	public R upgradeScope(Scope scope) {
		if (scope == getScope()) {
			return self();
		}
		return rescope(Rescoper.upgradeRescoper(getScope(), scope));
	}

	public R rescope(Scope scope) {
		if (getScope() == scope) {
			return self();
		}
		return rescope(getScope().rescoperTo(scope));
	}

	public final CompilerLogger getLogger() {
		return getScope().getLogger();
	}

	public abstract void resolveAll();

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
		Scoped.assertSameScope(this, other);
	}

	@SuppressWarnings("unchecked")
	protected final R self() {
		return (R) this;
	}

	protected abstract R create(
			Rescoper rescoper,
			Rescoper additionalRescoper);

}
