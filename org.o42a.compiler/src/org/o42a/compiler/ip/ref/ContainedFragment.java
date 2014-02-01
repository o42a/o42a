/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref;

import org.o42a.core.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundFragment;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public abstract class ContainedFragment
		extends BoundFragment
		implements ContainerInfo {

	private final Location location;
	private final Container container;

	public ContainedFragment(LocationInfo location, Distributor distributor) {
		this.location = location.getLocation();
		this.container = distributor.getContainer();
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	@Override
	public final Scope getScope() {
		return getContainer().getScope();
	}

	@Override
	public final Container getContainer() {
		return this.container;
	}

	public final CompilerLogger getLogger() {
		return getLocation().getLogger();
	}

	@Override
	public final Distributor distribute() {
		return Contained.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Contained.distributeIn(this, container);
	}

	public final Ref toRef() {
		return Path.SELF_PATH
				.bind(this, getScope())
				.append(this)
				.target(distribute());
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
		out.append(getScope()).append('@').append(this.location.getContext());

		final Loggable loggable = this.location.getLoggable();

		if (loggable != null) {
			out.append("]:[");
			loggable.print(out);
		}
		out.append(']');

		return out.toString();
	}

}
