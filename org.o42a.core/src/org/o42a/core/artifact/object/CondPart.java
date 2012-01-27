/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.artifact.object;

import org.o42a.core.def.CondDef;
import org.o42a.core.def.CondDefs;
import org.o42a.core.def.DefKind;


public class CondPart extends ObjectValuePart<CondDef, CondDefs> {

	CondPart(ObjectValue objectValue, DefKind defKind) {
		super(objectValue, defKind);
		assert !defKind.isValue() :
			"Condition definition kind expected, but was " + defKind;
	}

}
