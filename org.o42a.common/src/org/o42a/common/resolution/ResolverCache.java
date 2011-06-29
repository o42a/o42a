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
package org.o42a.common.resolution;

import static org.o42a.util.use.Usable.simpleUsable;

import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.ref.Resolver;
import org.o42a.util.use.Usable;


public class ResolverCache {

	private final String name;
	private final Object used;
	private IdentityHashMap<Scope, Usable<?>> cache;

	public ResolverCache(String name, Object used) {
		this.name = name;
		this.used = used;
	}

	public final boolean isEmpty() {
		return this.cache == null;
	}

	public Resolver resolve(Resolver resolver) {

		final Scope scope = resolver.getScope();

		if (this.cache == null) {
			this.cache = new IdentityHashMap<Scope, Usable<?>>();
		} else {

			final Usable<?> cached = this.cache.get(scope);

			if (cached != null) {
				cached.useBy(resolver);
				System.err.println("(!) " + this.cache.size());
				return null;
			}
		}

		final Usable<?> usable = simpleUsable(this.name, this.used);

		usable.useBy(resolver);
		this.cache.put(scope, usable);

		return scope.newResolver(usable);
	}

	@Override
	public String toString() {
		if (this.name == null) {
			if (this.used == null) {
				return super.toString();
			}
			return "ResolverCache[" + this.used + ']';
		}
		if (this.used == null) {
			return "ResolverCache[" + this.name + ']';
		}
		return "ResolverCache[" + this.name + ": " + this.used + ']';
	}

}
