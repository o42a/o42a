/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public class Contained extends Scoped implements ContainerInfo {

	public static Distributor distribute(ContainerInfo contained) {
		return new ContainedDistributor(contained);
	}

	public static Distributor distributeIn(
			LocationInfo location,
			Container container) {
		return new OtherContainerDistributor(container, location);
	}

	private final Container container;

	public Contained(LocationInfo location, Distributor distributor) {
		super(location, distributor.getScope());
		this.container = distributor.getContainer();
		if (this.container != null) {
			assert this.container.getScope().is(getScope()) :
				this.container + " should be in scope " + getScope();
		}
	}

	public Contained(
			CompilerContext context,
			LogInfo location,
			Distributor distributor) {
		super(context, location, distributor.getScope());
		this.container = distributor.getContainer();
		if (this.container != null) {
			assert this.container.getScope().is(getScope()) :
				this.container + " should be in scope " + getScope();
		}
	}

	@Override
	public Container getContainer() {
		return this.container;
	}

	@Override
	public Distributor distribute() {
		return distribute(this);
	}

	@Override
	public Distributor distributeIn(Container container) {
		return distributeIn(this, container);
	}

	private static final class ContainedDistributor extends Distributor {

		private final ContainerInfo contained;

		private ContainedDistributor(ContainerInfo contained) {
			this.contained = contained;
		}

		@Override
		public Location getLocation() {
			return this.contained.getLocation();
		}

		@Override
		public Scope getScope() {
			return this.contained.getScope();
		}

		@Override
		public Container getContainer() {
			return this.contained.getContainer();
		}

	}

	private static final class OtherContainerDistributor extends Distributor {

		private final Container container;
		private final Location location;

		OtherContainerDistributor(Container container, LocationInfo location) {
			this.container = container;
			this.location = location.getLocation();
		}

		@Override
		public Location getLocation() {
			return this.location.getLocation();
		}

		@Override
		public Scope getScope() {
			return this.container.getScope();
		}

		@Override
		public Container getContainer() {
			return this.container;
		}

	}

}
