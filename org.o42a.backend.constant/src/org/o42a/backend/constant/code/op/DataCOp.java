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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Type;


public final class DataCOp extends PtrCOp<DataOp, Ptr<DataOp>>
		implements DataOp {

	public DataCOp(OpBE<DataOp> backend, AllocClass allocClass) {
		super(backend, allocClass);
	}

	public DataCOp(
			OpBE<DataOp> backend,
			AllocClass allocClass,
			Ptr<DataOp> constant) {
		super(backend, allocClass, constant);
	}

	@Override
	public <S extends StructOp<S>> S to(
			final CodeId id,
			final Code code,
			final Type<S> type) {

		final CCode<?> ccode = cast(code);

		return type.op(new CStruct<S>(
				new OpBE<S>(id, ccode) {
					@Override
					protected S write() {
						return backend().underlying().to(
								getId(),
								part().underlying(),
								getBackend().underlying(type));
					}
				},
				getAllocClass(),
				type));
	}

	@Override
	public DataOp create(OpBE<DataOp> backend, Ptr<DataOp> constant) {
		return new DataCOp(backend, null, constant);
	}

}
