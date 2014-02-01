/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
import org.o42a.core.source.Location;


/**
 * Member access.
 *
 * <p>Contains a complete information about the user accessing a member.
 * Containers may decide which members are visible based on this information.
 * </p>
 */
public final class Access implements ContainerInfo {

	private final ContainerInfo user;
	private final Accessor accessor;
	private final AccessSource source;

	Access(ContainerInfo user, Accessor accessor, AccessSource source) {
		this.user = user;
		this.accessor = accessor;
		this.source = source;
	}

	private Access(Access prototype, Accessor accessor) {
		this.user = prototype.user;
		this.accessor = accessor;
		this.source = prototype.source;
	}

	private Access(Access prototype, AccessSource source) {
		this.user = prototype.user;
		this.accessor = prototype.accessor;
		this.source = source;
	}

	public final Accessor getAccessor() {
		return this.accessor;
	}

	public final Access setAccessor(Accessor accessor) {
		assert accessor != null :
			"Accessor not specified";
		return new Access(this, accessor);
	}

	public final AccessSource getSource() {
		return this.source;
	}

	public final Access setSource(AccessSource source) {
		assert source != null :
			"Access source not specified";
		return new Access(this, source);
	}

	@Override
	public final Location getLocation() {
		return this.user.getLocation();
	}

	@Override
	public final Scope getScope() {
		return this.user.getScope();
	}

	@Override
	public final Container getContainer() {
		return this.user.getContainer();
	}

	@Override
	public final Distributor distribute() {
		return Contained.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Contained.distributeIn(this, container);
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
		if (this.user == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("Access[by ").append(this.user);
		if (this.accessor != null) {
			out.append(" as ").append(this.accessor);
		}
		out.append(']');

		return out.toString();
	}
}
