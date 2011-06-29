/*
    Modules Commons
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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Statement;
import org.o42a.util.use.UserInfo;


public class StatementResolver {

	private ResolverCache fullResolverCache;
	private ResolverCache valueResolverCache;

	public void resolveAll(Resolver resolver, Statement... statements) {
		if (this.fullResolverCache == null) {
			this.fullResolverCache = new ResolverCache("FullResolver", this);
		}

		final Resolver fullResolver = this.fullResolverCache.resolve(resolver);

		if (fullResolver == null) {
			return;
		}
		for (Statement statement : statements) {
			statement.resolveAll(fullResolver);
		}
	}

	public final void resolveBuiltin(Obj object, Statement... statements) {

		final UserInfo user = object.value(dummyUser());
		final Resolver resolver = object.getScope().newResolver(user);

		resolveValues(resolver, statements);
	}

	public final void resolveValues(
			Resolver resolver,
			Statement... statements) {
		if (this.valueResolverCache == null) {
			this.valueResolverCache = new ResolverCache("ValueResolver", this);
		}

		final Resolver valueResolver =
				this.valueResolverCache.resolve(resolver);

		if (valueResolver == null) {
			return;
		}
		for (Statement statement : statements) {
			statement.resolveValues(valueResolver);
		}
	}

}
