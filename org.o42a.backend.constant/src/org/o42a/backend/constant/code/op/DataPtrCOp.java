/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataPtrOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Ptr;


public abstract class DataPtrCOp<P extends DataPtrOp<P>>
		extends MemPtrCOp<P>
		implements DataPtrOp<P> {

	public DataPtrCOp(OpBE<P> backend, AllocClass allocClass) {
		super(backend, allocClass);
	}

	public DataPtrCOp(OpBE<P> backend, AllocClass allocClass, Ptr<P> constant) {
		super(backend, allocClass, constant);
	}

	@Override
	public DataOp toData(CodeId id, Code code) {

		final CodeId castId = code.getOpNames().castId(id, "data", this);

		return new DataCOp(
				new OpBE<DataOp>(castId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected DataOp write() {
						return backend().underlying().toData(
								getId(),
								part().underlying());
					}
				},
				getAllocClass());
	}

}
