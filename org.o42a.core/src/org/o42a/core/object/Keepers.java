/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object;

import org.o42a.core.object.state.Keeper;
import org.o42a.core.object.state.ObjectKeepers;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;


public final class Keepers extends ObjectKeepers {

	private int keeperNameSeq;

	Keepers(Obj object) {
		super(object);
	}

	public final Keeper addKeeper(LocationInfo location, Ref ref) {
		assert getObject().getContext().fullResolution().assertIncomplete();
		return declareKeeper(
				location,
				ref,
				Integer.toString(++this.keeperNameSeq));
	}

}
