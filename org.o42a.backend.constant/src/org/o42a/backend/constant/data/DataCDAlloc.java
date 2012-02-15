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
package org.o42a.backend.constant.data;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.op.DataCOp;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.data.backend.DataAllocation;


public final class DataCDAlloc extends CDAlloc<DataOp, Data<DataOp>> {

	public DataCDAlloc(
			ConstBackend backend,
			Ptr<DataOp> pointer,
			UnderAlloc<DataOp> underlAlloc) {
		super(backend, pointer, underlAlloc);
	}

	@Override
	public DataCOp op(CodeId id, AllocClass allocClass, CodeWriter writer) {
		return new DataCOp(
				new OpBE<DataOp>(id, cast(writer)) {
					@Override
					public void prepare() {
					}
					@Override
					protected DataOp write() {
						return getUnderlyingPtr().op(
								getId(),
								part().underlying());
					}
				},
				allocClass,
				getPointer());
	}

	@Override
	public TopLevelCDAlloc<?> getTopLevel() {
		return null;
	}

	@Override
	public ContainerCDAlloc<?> getEnclosing() {
		return null;
	}

	@Override
	public DataAllocation<DataOp> toData() {
		return this;
	}

	@Override
	protected Data<DataOp> allocateUnderlying(SubData<?> container) {
		throw new UnsupportedOperationException();
	}

}
