/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.PathWalker;


public abstract class ResolverFactory {

	private final Scope scope;
	private Resolver resolver;

	public ResolverFactory(Scope scope) {
		this.scope = scope;
	}

	public final Scope getScope() {
		return this.scope;
	}

	public final Resolver resolver() {
		if (this.resolver != null) {
			return this.resolver;
		}
		return this.resolver = createResolver(DUMMY_PATH_WALKER);
	}

	public final Resolver walkingResolver(PathWalker walker) {
		if (walker == null || walker == DUMMY_PATH_WALKER) {
			return resolver();
		}
		return createResolver(walker);
	}

	@Override
	public String toString() {
		if (this.scope == null) {
			return super.toString();
		}
		return "ResolverFactory[" + this.scope + ']';
	}

	protected abstract Resolver createResolver(PathWalker walker);

}
