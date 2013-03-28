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
package org.o42a.core.member;

import org.o42a.core.*;
import org.o42a.core.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;


public abstract class MemberOwner implements PlaceInfo {

	private final Container container;

	public MemberOwner(Container container) {
		this.container = container;
	}

	@Override
	public final Location getLocation() {
		return this.container.getLocation();
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	@Override
	public final Container getContainer() {
		return this.container;
	}

	@Override
	public final Scope getScope() {
		return this.container.getScope();
	}

	@Override
	public final ScopePlace getPlace() {
		return getScope().getPlace();
	}

	public abstract Obj getOwner();

	public final Obj toObject() {
		return getContainer().toObject();
	}

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
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
		if (this.container == null) {
			return super.toString();
		}
		return this.container.toString();
	}

}
