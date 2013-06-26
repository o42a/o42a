/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.value.impl;

import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;

import org.o42a.core.ir.object.*;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.ir.value.type.ValueTypeIR;


public final class DefaultValueIR extends ValueIR {

	public DefaultValueIR(ValueTypeIR<?> valueTypeIR, ObjectIR objectIR) {
		super(valueTypeIR, objectIR);
	}

	@Override
	public void allocateBody(ObjectIRBodyData data) {
	}

	@Override
	public void setInitialValue(ObjectTypeIR data) {
		data.getInstance().data().value().set(INDEFINITE_VAL);
	}

	@Override
	public ValueOp op(ObjectOp object) {
		return defaultOp(object);
	}

}
