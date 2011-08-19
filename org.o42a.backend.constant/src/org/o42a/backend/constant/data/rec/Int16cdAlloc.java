/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011 Ruslan Lopatin

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
import org.o42a.backend.constant.code.rec.Int16recCOp;
import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.RecCDAlloc;
import org.o42a.codegen.code.op.Int16recOp;
import org.o42a.codegen.data.Int16rec;
import org.o42a.codegen.data.SubData;


public final class Int16cdAlloc
		extends RecCDAlloc<Int16rec, Int16recOp, Short> {

	public Int16cdAlloc(
			ContainerCDAlloc<?> enclosing,
			Int16rec data,
			CDAlloc<Int16recOp, Int16rec> type) {
		super(enclosing, data, type);
	}

	@Override
	protected Int16recCOp op(CCode<?> code, Int16recOp underlying) {
		return new Int16recCOp(code, underlying);
	}

	@Override
	protected Int16rec allocateUnderlying(SubData<?> container, String name) {
		return container.addInt16(name);
	}

}
