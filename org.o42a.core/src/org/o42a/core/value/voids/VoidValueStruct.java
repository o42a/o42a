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
package org.o42a.core.value.voids;


import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.value.SingleValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;


public class VoidValueStruct extends SingleValueStruct<Void> {

	public static final VoidValueStruct INSTANCE = new VoidValueStruct();

	private VoidValueStruct() {
		super(ValueType.VOID, Void.class);
	}

	@Override
	protected ValueStructIR<SingleValueStruct<Void>, Void> createIR(
			Generator generator) {
		return new VoidValueStructIR(generator, this);
	}

}