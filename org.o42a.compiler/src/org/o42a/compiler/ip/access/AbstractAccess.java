/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.access;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.Scoped;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;


public abstract class AbstractAccess<T extends ScopeInfo> implements ScopeInfo {

	private final AccessRules rules;
	private final T target;

	public AbstractAccess(AccessRules rules, T target) {
		this.rules = rules;
		this.target = target;
	}

	public final AccessRules getRules() {
		return this.rules;
	}

	public final T get() {
		return this.target;
	}

	@Override
	public final Location getLocation() {
		return get().getLocation();
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	@Override
	public final Scope getScope() {
		return get().getScope();
	}

	public final CompilerLogger getLogger() {
		return getLocation().getLogger();
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
		if (this.target == null) {
			return super.toString();
		}
		return this.target.toString();
	}

}
