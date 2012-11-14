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
package org.o42a.core.value.array.impl;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;


public final class ArrayValueTypeIR extends ValueTypeIR<Array> {

	public ArrayValueTypeIR(Generator generator, ArrayValueType valueType) {
		super(generator, valueType);
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		if (!getValueType().isVariable()) {
			return defaultValueIR(objectIR);
		}
		return new ArrayValueIR(this, objectIR);
	}

	@Override
	protected ArrayStaticsIR createStaticsIR() {
		return new ArrayStaticsIR(this);
	}

}
