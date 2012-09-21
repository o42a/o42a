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
package org.o42a.core.value.floats;

import static org.o42a.core.ir.IRNames.CONST_ID;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.struct.AbstractSingleValueStructIR;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.object.state.Keeper;
import org.o42a.util.string.ID;


final class FloatValueStructIR
		extends AbstractSingleValueStructIR<Double> {

	private int constSeq;

	FloatValueStructIR(Generator generator, FloatValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public Val val(Double value) {
		return new Val(value);
	}

	@Override
	public KeeperIR<?, ?> createKeeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		return new FloatKeeperIR(this, bodyIR, keeper);
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		return defaultValueIR(objectIR);
	}

	@Override
	protected ID constId(Double value) {
		return CONST_ID.sub("FLOAT").anonymous(++this.constSeq);
	}

}