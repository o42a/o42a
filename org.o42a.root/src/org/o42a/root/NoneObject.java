/*
    Root Object Definition
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.root;

import static org.o42a.core.object.ConstructionMode.PREDEFINED_CONSTRUCTION;

import org.o42a.core.Scope;
import org.o42a.core.object.ConstructionMode;
import org.o42a.util.string.ID;


public final class NoneObject extends FalseObject {

	public NoneObject(Scope topScope) {
		super(topScope, ID.id("NONE"));
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return PREDEFINED_CONSTRUCTION;
	}

	@Override
	public String toString() {
		return "NONE";
	}

}