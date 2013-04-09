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

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Sentence;


public final class StatementsDistributor extends Distributor {

	private final Location location;
	private final Sentence<?> sentence;

	public StatementsDistributor(
			LocationInfo location,
			Sentence<?> sentence) {
		this.location = location.getLocation();
		this.sentence = sentence;
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	@Override
	public Scope getScope() {
		return this.sentence.getScope();
	}

	@Override
	public Container getContainer() {
		return this.sentence.getContainer();
	}

}
