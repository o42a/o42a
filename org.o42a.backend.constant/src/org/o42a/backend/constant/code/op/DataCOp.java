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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;


public final class DataCOp extends PtrCOp<DataOp> implements DataOp {

	public DataCOp(CCode<?> code, DataOp underlying) {
		super(code, underlying);
	}

	@Override
	public <S extends StructOp<S>> S to(CodeId id, Code code, Type<S> type) {

		final CCode<?> ccode = cast(code);
		final S underlyingStruct = getUnderlying().to(
				id,
				ccode.getUnderlying(),
				getBackend().underlying(type));

		return type.op(new CStruct<S>(ccode, underlyingStruct, type));
	}

	@Override
	public DataCOp create(CCode<?> code, DataOp underlying) {
		return new DataCOp(code, underlying);
	}

}
