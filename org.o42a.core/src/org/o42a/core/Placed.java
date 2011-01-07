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

import org.o42a.util.log.LogInfo;


public class Placed extends Scoped implements PlaceSpec {

	public static Distributor distribute(PlaceSpec placed) {
		return new PlaceDistributor(placed);
	}

	public static Distributor distributeIn(
			PlaceSpec placed,
			Container container) {
		return new OtherContainerDistributor(container, placed);
	}

	private final ScopePlace place;
	private final Container container;

	public Placed(LocationSpec location, Distributor distributor) {
		super(location, distributor.getScope());
		this.place = distributor.getPlace();
		this.container = distributor.getContainer();
		if (this.container != null) {
			assert this.container.getScope() == getScope() :
				this.container + " should be in scope " + getScope();
		}
	}

	public Placed(
			CompilerContext context,
			LogInfo location,
			Distributor distributor) {
		super(context, location, distributor.getScope());
		this.place = distributor.getPlace();
		this.container = distributor.getContainer();
		if (this.container != null) {
			assert this.container.getScope() == getScope() :
				this.container + " should be in scope " + getScope();
		}
	}

	@Override
	public final ScopePlace getPlace() {
		return this.place;
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

	private static final class PlaceDistributor extends Distributor {

		private final PlaceSpec placed;

		private PlaceDistributor(PlaceSpec placed) {
			this.placed = placed;
		}

		@Override
		public Scope getScope() {
			return this.placed.getScope();
		}

		@Override
		public Container getContainer() {
			return this.placed.getContainer();
		}

		@Override
		public ScopePlace getPlace() {
			return this.placed.getPlace();
		}

	}

	private static final class OtherContainerDistributor extends Distributor {

		private final Container container;
		private final PlaceSpec placed;

		private OtherContainerDistributor(
				Container container,
				PlaceSpec placed) {
			this.container = container;
			this.placed = placed;
		}

		@Override
		public Scope getScope() {
			return this.container.getScope();
		}

		@Override
		public Container getContainer() {
			return this.container;
		}

		@Override
		public ScopePlace getPlace() {
			return this.placed.getPlace();
		}

	}

}
