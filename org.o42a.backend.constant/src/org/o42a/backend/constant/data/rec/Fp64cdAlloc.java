/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.backend.constant.code.rec.RecStore.allocRecStore;

import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.rec.Fp64recCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.Fp64recOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.Fp64rec;
import org.o42a.codegen.data.SubData;


public final class Fp64cdAlloc extends RecCDAlloc<Fp64rec, Fp64recOp, Double> {

	public Fp64cdAlloc(
			ContainerCDAlloc<?> enclosing,
			Fp64rec data,
			Fp64cdAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	public Double underlyingValue(Double value) {
		return value;
	}

	@Override
	protected Fp64rec allocateUnderlying(SubData<?> container, String name) {
		return container.addFp64(name);
	}

	@Override
	protected Fp64recOp op(OpBE<Fp64recOp> backend, AllocPlace allocPlace) {
		return new Fp64recCOp(backend, allocRecStore(allocPlace), getPointer());
	}

}
