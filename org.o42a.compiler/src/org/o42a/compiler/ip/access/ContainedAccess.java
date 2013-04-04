/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.access;

import org.o42a.core.*;


public abstract class ContainedAccess<T extends ContainerInfo>
		extends AbstractAccess<T>
		implements ContainerInfo {

	public ContainedAccess(AccessRules rules, T target) {
		super(rules, target);
	}

	@Override
	public final Container getContainer() {
		return get().getContainer();
	}

	@Override
	public final Distributor distribute() {
		return Contained.distribute(this);
	}

	public final AccessDistributor distributeAccess() {
		return getRules().distribute(distribute());
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Contained.distributeIn(this, container);
	}

}
