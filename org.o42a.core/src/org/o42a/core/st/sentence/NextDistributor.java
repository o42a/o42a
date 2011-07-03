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
package org.o42a.core.st.sentence;

import static org.o42a.core.ScopePlace.localPlace;

import org.o42a.core.*;
import org.o42a.util.Place;
import org.o42a.util.log.Loggable;


final class NextDistributor extends Distributor {

	private final Statements<?> statements;
	private final Container container;
	private final LocalPlace place;

	NextDistributor(
			Statements<?> statements,
			Container container,
			Place place) {
		this.statements = statements;
		this.container = container;
		this.place = localPlace(
				this.statements.getScope().toLocal(),
				place);
	}

	@Override
	public CompilerContext getContext() {
		return this.statements.getContext();
	}

	@Override
	public Loggable getLoggable() {
		return this.statements.getLoggable();
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
