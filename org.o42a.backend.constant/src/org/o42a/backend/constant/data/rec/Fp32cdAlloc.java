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
import org.o42a.backend.constant.code.rec.Fp32recCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.Fp32recOp;
import org.o42a.codegen.data.Fp32rec;
import org.o42a.codegen.data.SubData;


public final class Fp32cdAlloc extends RecCDAlloc<Fp32rec, Fp32recOp, Float> {

	public Fp32cdAlloc(
			ContainerCDAlloc<?> enclosing,
			Fp32rec data,
			Fp32cdAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	protected Fp32recCOp op(CCode<?> code, Fp32recOp underlying) {
		return new Fp32recCOp(code, underlying, getPointer());
	}

	@Override
	protected Fp32rec allocateUnderlying(SubData<?> container, String name) {
		return container.addFp32(name, this);
	}

}
