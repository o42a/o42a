/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.core.source.Location;


public abstract class Distributor implements ContainerInfo {

	public static Distributor containerDistributor(Container container) {
		return new ContainerDistributor(container);
	}

	@Override
	public final Distributor distribute() {
		return this;
	}

	@Override
	public String toString() {
		return "Distributor[" + getContainer() + ']';
	}

	private static final class ContainerDistributor extends Distributor {

		private final Container container;

		ContainerDistributor(Container container) {
			this.container = container;
		}

		@Override
		public Location getLocation() {
			return this.container.getLocation();
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
