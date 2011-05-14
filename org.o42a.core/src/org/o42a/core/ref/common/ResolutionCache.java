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
package org.o42a.core.ref.common;

import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.ref.Resolver;
import org.o42a.util.use.Usable;


public abstract class ResolutionCache<T, U extends Usable<T>> {

	private IdentityHashMap<Scope, U> cache;

	public final boolean isEmpty() {
		return this.cache == null;
	}

	public final U get(Scope scope) {
		if (isEmpty()) {
			return null;
		}
		return this.cache.get(scope);
	}

	public T resolve(Resolver resolver) {

		final Scope scope = resolver.getScope();

		if (this.cache == null) {
			this.cache = new IdentityHashMap<Scope, U>(1);
		} else {

			final U cached = this.cache.get(scope);

			if (cached != null) {
				return cached.useBy(resolver);
			}
		}

		final U usable = createUsable(resolver);

		this.cache.put(scope, usable);

		return usable.useBy(resolver);
	}

	protected abstract U createUsable(Resolver resolver);

}
