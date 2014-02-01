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
package org.o42a.compiler.ip.file;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;


public final class OtherContextDistributor extends Distributor {

	public static Distributor distributeIn(
			Distributor distributor,
			CompilerContext context) {
		if (distributor.getContext() == context) {
			return distributor;
		}
		return new OtherContextDistributor(context, distributor);
	}

	private final Location location;
	private final Distributor distributor;

	private OtherContextDistributor(
			CompilerContext context,
			Distributor distributor) {
		this.location = new Location(context, distributor.getLocation());
		this.distributor = distributor;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public Container getContainer() {
		return this.distributor.getContainer();
	}

	@Override
	public Scope getScope() {
		return this.distributor.getScope();
	}

}
