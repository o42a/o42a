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
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.util.use.Usable;
import org.o42a.util.use.UsableUser;
import org.o42a.util.use.User;


final class ExpressionCache {

	private IdentityHashMap<Scope, UsableExpression> cache;
	private final Expression expression;

	ExpressionCache(Expression expression) {
		this.expression = expression;
	}

	public final Resolution getResolution() {
		return resolution(this.expression.getScope());
	}

	public final boolean isEmpty() {
		return this.cache == null;
	}

	public final int size() {
		return this.cache.size();
	}

	public final Resolution resolution(Scope scope) {

		final UsableExpression cached = this.cache.get(scope);

		if (cached == null) {
			return null;
		}

		return cached.getResolution();
	}

	public Resolution resolve(Resolver resolver) {

		final Scope scope = resolver.getScope();

		if (this.cache == null) {
			this.cache = new IdentityHashMap<Scope, UsableExpression>();
		} else {

			final UsableExpression cached = this.cache.get(scope);

			if (cached != null) {
				cached.useBy(resolver);
				return cached.getResolution();
			}
		}

		final UsableExpression usable =
				new UsableExpression(this.expression, resolver);

		this.cache.put(scope, usable);
		usable.useBy(resolver);

		return usable.getResolution();
	}

	@Override
	public String toString() {
		if (this.expression == null) {
			return super.toString();
		}
		return getClass().getSimpleName() + '[' + this.expression + ']';
	}

	private static final class UsableExpression extends Usable {

		private final Expression expression;
		private final UsableUser user;
		private final Resolution resolution;

		UsableExpression(Expression expression, Resolver resolver) {
			this.expression = expression;
			this.user = new UsableUser(this);

			final Resolver expressionResolver =
					resolver.getScope().newResolver(this);

			this.resolution = expression.resolveBy(expressionResolver);
		}

		public final Resolution getResolution() {
			return this.resolution;
		}

		@Override
		public final User toUser() {
			return this.user;
		}

		@Override
		public String toString() {
			return "Expression[" + this.expression + ']';
		}

	}

}
