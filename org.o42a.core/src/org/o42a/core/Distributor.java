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
package org.o42a.core;

import static org.o42a.core.ScopePlace.scopePlace;

import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.Loggable;


public abstract class Distributor implements PlaceInfo {

	public static Distributor declarativeDistributor(Container container) {
		return new DeclarativeDistributor(container);
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Distributor distribute() {
		return this;
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
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

		final Scope scope = getScope();
		final ScopePlace place = getPlace();

		if (scope == place.getAppearedIn()) {
			return "Distributor[" + place + ']';
		}

		return "Distributor[" + place + " in " + scope + ']';
	}

	private static final class DeclarativeDistributor extends Distributor {

		private final ScopePlace place;
		private final Container container;

		DeclarativeDistributor(Container container) {
			this.container = container;
			this.place = scopePlace(container.getScope());
		}

		@Override
		public Loggable getLoggable() {
			return this.container.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.container.getContext();
		}

		@Override
		public Scope getScope() {
			return this.place.getAppearedIn();
		}

		@Override
		public Container getContainer() {
			return this.container;
		}

		@Override
		public ScopePlace getPlace() {
			return this.place;
		}

	}

}
