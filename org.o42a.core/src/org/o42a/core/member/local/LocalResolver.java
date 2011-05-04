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
package org.o42a.core.member.local;

import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.ResolverFactory;
import org.o42a.util.use.UserInfo;


public class LocalResolver extends Resolver {

	LocalResolver(LocalScope scope, UserInfo user) {
		super(scope, user);
	}

	public final LocalScope getLocal() {
		return getScope().toLocal();
	}

	@Override
	public LocalResolver newResolver(UserInfo user) {
		return getLocal().newResolver(user);
	}

	@Override
	public String toString() {
		return "LocalResolver[" + getScope() + ']';
	}

	private static final class DummyLocalResolver extends LocalResolver {

		DummyLocalResolver(LocalScope scope) {
			super(scope, dummyResolverUser());
		}

		@Override
		public LocalResolver newResolver(UserInfo user) {
			return this;
		}

		@Override
		public String toString() {
			return "DummyLocalResolver[" + getScope() + ']';
		}

	}

	static final class Factory extends ResolverFactory<LocalResolver> {

		Factory(LocalScope scope) {
			super(scope);
		}

		@Override
		protected LocalResolver dummyResolver() {
			return new DummyLocalResolver(getScope().toLocal());
		}

		@Override
		protected LocalResolver createResolver() {
			return new LocalResolver(getScope().toLocal(), this);
		}

	}

}
