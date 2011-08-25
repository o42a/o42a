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
import org.o42a.backend.constant.code.rec.RelRecCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.data.RelPtr;
import org.o42a.codegen.data.RelRec;
import org.o42a.codegen.data.SubData;


public final class RelRecCDAlloc extends RecCDAlloc<RelRec, RelRecOp, RelPtr> {

	public RelRecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			RelRec data,
			RelRecCDAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	protected RelRecCOp op(CCode<?> code, RelRecOp underlying) {
		return new RelRecCOp(code, underlying, getPointer());
	}

	@Override
	protected RelRec allocateUnderlying(SubData<?> container, String name) {
		return container.addRelPtr(name, this);
	}

}
