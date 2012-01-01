/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.data.rec;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.rec.Int8recCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.codegen.data.Int8rec;
import org.o42a.codegen.data.SubData;


public final class Int8cdAlloc extends RecCDAlloc<Int8rec, Int8recOp, Byte> {

	public Int8cdAlloc(
			ContainerCDAlloc<?> enclosing,
			Int8rec data,
			Int8cdAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	protected Int8recCOp op(CCode<?> code, Int8recOp underlying) {
		return new Int8recCOp(code, underlying, getPointer());
	}

	@Override
	protected Int8rec allocateUnderlying(SubData<?> container, String name) {
		return container.addInt8(name, this);
	}

}
