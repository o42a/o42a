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
import org.o42a.backend.constant.code.rec.AnyRecCOp;
import org.o42a.backend.constant.data.AnyCDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.data.*;


public final class AnyRecCDAlloc
		extends PtrRecCDAlloc<AnyRec, AnyRecOp, Ptr<AnyOp>> {

	public AnyRecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			AnyRec data,
			AnyRecCDAlloc typeAllocation) {
		super(enclosing, data, typeAllocation);
		nest();
	}

	@Override
	public Ptr<AnyOp> underlyingValue(final Ptr<AnyOp> value) {

		final AnyCDAlloc alloc = (AnyCDAlloc) value.get().getAllocation();

		return alloc.getUnderlyingPtr();
	}

	@Override
	protected AnyRec allocateUnderlying(SubData<?> container, String name) {
		return container.addPtr(name);
	}

	@Override
	protected AnyRecOp op(OpBE<AnyRecOp> backend, AllocPlace allocPlace) {
		return new AnyRecCOp(backend, allocRecStore(allocPlace), getPointer());
	}

}
