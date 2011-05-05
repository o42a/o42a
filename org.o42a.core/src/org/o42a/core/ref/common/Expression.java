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
package org.o42a.core.ref.common;

import static org.o42a.util.use.Usable.simpleUsable;

import java.util.IdentityHashMap;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.Usable;


public abstract class Expression extends Ref {

	private final Usable<Expression> usable =
		simpleUsable("UsableExpression", this);
	private Resolution resolved;
	private IdentityHashMap<Scope, Resolution> cache;

	public Expression(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public final Resolution resolve(Resolver resolver) {
		usable().useBy(resolver);

		final Scope scope = resolver.getScope();

		resolver = resolver.newResolver(usable());
		if (scope == getScope()) {
			if (this.resolved != null) {
				return this.resolved;
			}
			return this.resolved = doResolveExpression(resolver);
		}

		assertCompatible(scope);

		if (this.cache == null) {
			this.cache = new IdentityHashMap<Scope, Resolution>(2);
		} else {

			final Resolution cached = this.cache.get(scope);

			if (cached != null) {
				return cached;
			}
		}

		final Resolution resolution = doResolveExpression(resolver);

		this.cache.put(scope, resolution);

		return resolution;
	}

	public final Resolution getResolved() {
		return this.resolved;
	}

	@Override
	public String toString() {

		final Resolution resolved = getResolved();

		if (resolved != null) {
			return resolved.toString();
		}

		final Loggable loggable = getLoggable();

		if (loggable != null) {

			final StringBuilder out = new StringBuilder();

			out.append('[');
			loggable.printContent(out);
			out.append("]");

			return out.toString();
		}

		return getClass().getSimpleName();
	}

	protected abstract Resolution resolveExpression(Resolver resolver);

	protected final Usable<?> usable() {
		return this.usable;
	}

	private final Resolution doResolveExpression(Resolver resolver) {

		final Resolution resolution = resolveExpression(resolver);

		if (resolution != null) {
			return resolution;
		}

		return noResolution();
	}

}
