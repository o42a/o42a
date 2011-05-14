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

import org.o42a.core.Scope;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.util.use.Usable;
import org.o42a.util.use.User;


final class ExpressionCache
		extends ResolutionCache<Resolution, ExpressionCache.UsableExpression> {

	private final Expression expression;

	ExpressionCache(Expression expression) {
		this.expression = expression;
	}

	public final Resolution getResolution() {
		return resolution(this.expression.getScope());
	}

	public final Resolution resolution(Scope scope) {

		final UsableExpression usable = get(scope);

		if (usable == null) {
			return null;
		}

		return usable.getResolution();
	}

	@Override
	public String toString() {
		if (this.expression == null) {
			return super.toString();
		}
		return getClass().getSimpleName() + '[' + this.expression + ']';
	}

	@Override
	protected UsableExpression createUsable(Resolver resolver) {
		return new UsableExpression(this.expression, resolver);
	}

	static final class UsableExpression extends Usable<Resolution> {

		private final Expression expression;
		private final Resolution resolution;

		UsableExpression(Expression expression, Resolver resolver) {
			this.expression = expression;
			resolver = resolver.getScope().newResolver(this);
			this.resolution = expression.resolveBy(resolver);
		}

		public final Resolution getResolution() {
			return this.resolution;
		}

		@Override
		public String toString() {
			return "Expression[" + this.expression + ']';
		}

		@Override
		protected Resolution createUsed(User user) {
			return this.resolution;
		}

	}

}
