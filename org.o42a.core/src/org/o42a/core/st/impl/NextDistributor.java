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
package org.o42a.core.st.impl;

import static org.o42a.core.ScopePlace.localPlace;

import org.o42a.core.*;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.Place;


public final class NextDistributor extends Distributor {

	private final Statements<?, ?> statements;
	private final Container container;
	private final LocalPlace place;

	public NextDistributor(
			Statements<?, ?> statements,
			Container container,
			Place place) {
		this.statements = statements;
		this.container = container;
		this.place =
				localPlace(this.statements.getScope().toLocalScope(), place);
	}

	@Override
	public Location getLocation() {
		return this.statements.getLocation();
	}

	@Override
	public ScopePlace getPlace() {
		return this.place;
	}

	@Override
	public Container getContainer() {
		return this.container;
	}

	@Override
	public Scope getScope() {
		return this.container.getScope();
	}

}
