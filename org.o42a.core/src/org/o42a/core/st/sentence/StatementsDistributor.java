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
import static org.o42a.core.ScopePlace.scopePlace;

import org.o42a.core.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.Place.Trace;
import org.o42a.util.log.Loggable;


final class StatementsDistributor extends Distributor {

	private final LocationInfo location;
	private final Sentence<?> sentence;
	private final ScopePlace place;

	StatementsDistributor(LocationInfo location, Sentence<?> sentence) {
		this.location = location;
		this.sentence = sentence;

		final Trace trace = this.sentence.getBlock().getTrace();

		if (trace == null) {
			this.place = scopePlace(getScope());
		} else {
			this.place = localPlace(getScope().toLocal(), trace.next());
		}
	}

	@Override
	public Loggable getLoggable() {
		return this.location.getLoggable();
	}

	@Override
	public CompilerContext getContext() {
		return this.location.getContext();
	}

	@Override
	public Scope getScope() {
		return this.sentence.getScope();
	}

	@Override
	public Container getContainer() {
		return this.sentence.getContainer();
	}

	@Override
	public ScopePlace getPlace() {
		return this.place;
	}

}
