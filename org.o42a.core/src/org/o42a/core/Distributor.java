/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.ast.Node;


public abstract class Distributor implements PlaceSpec {

	public static Distributor declarativeDistributor(Container container) {
		return new DeclarativeDistributor(container);
	}

	@Override
	public final CompilerContext getContext() {
		return getScope().getContext();
	}

	@Override
	public final Node getNode() {
		return getScope().getNode();
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
	public final void assertSameScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeSpec other) {
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
