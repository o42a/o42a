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
package org.o42a.core.member.local;

import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ResolverFactory;
import org.o42a.core.ref.path.PathWalker;


public class LocalResolver extends Resolver {

	LocalResolver(
			LocalResolverFactory factory,
			LocalScope scope,
			PathWalker walker) {
		super(factory, scope, walker);
	}

	@Override
	public String toString() {
		if (getScope() == null) {
			return super.toString();
		}
		return "LocalResolver[" + getScope() + ']';
	}

	static final class LocalResolverFactory
			extends ResolverFactory<LocalResolver> {

		LocalResolverFactory(LocalScope scope) {
			super(scope);
		}

		@Override
		protected LocalResolver createResolver(PathWalker walker) {
			return new LocalResolver(this, getScope().toLocalScope(), walker);
		}

	}

}
