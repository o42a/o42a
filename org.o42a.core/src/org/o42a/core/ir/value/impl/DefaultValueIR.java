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
package org.o42a.core.ir.value.impl;

import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueOp;


public final class DefaultValueIR extends ValueIR<DefaultValueIR.Op> {

	public DefaultValueIR(ObjectIR objectIR) {
		super(objectIR);
	}

	@Override
	public void allocateBody(ObjectBodyIR bodyIR, SubData<?> data) {
	}

	@Override
	public void allocateMethods(ObjectMethodsIR methodsIR, SubData<?> data) {
	}

	@Override
	public Op op(ObjectOp object) {
		return new Op(object);
	}

	public static final class Op extends ValueOp {

		Op(ObjectOp object) {
			super(object);
		}

	}

}
