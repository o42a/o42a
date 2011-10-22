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

import org.o42a.core.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public abstract class PlacedPathFragment
		extends PathFragment
		implements PlaceInfo {

	private final CompilerContext context;
	private final Loggable loggable;
	private final ScopePlace place;
	private final Container container;

	public PlacedPathFragment(LocationInfo location, Distributor distributor) {
		this.context = location.getContext();
		this.loggable = location.getLoggable();
		this.place = distributor.getPlace();
		this.container = distributor.getContainer();
	}

	@Override
	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	@Override
	public final Scope getScope() {
		return getContainer().getScope();
	}

	@Override
	public final ScopePlace getPlace() {
		return this.place;
	}

	@Override
	public final Container getContainer() {
		return this.container;
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	public Ref toRef() {
		return toPath().target(this, distribute());
	}

	public TypeRef toTypeRef() {
		return toPath().typeRef(this, distribute());
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

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName()).append('[');
		out.append(getScope()).append('@').append(getContext());

		final Loggable loggable = getLoggable();

		if (loggable != null) {
			out.append("]:[");
			loggable.printContent(out);
		}
		out.append(']');

		return out.toString();
	}

}
