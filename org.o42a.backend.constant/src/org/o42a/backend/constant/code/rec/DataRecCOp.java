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
package org.o42a.backend.constant.code.rec;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.op.DataCOp;
import org.o42a.backend.constant.data.DataCDAlloc;
import org.o42a.backend.constant.data.rec.DataRecCDAlloc;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.Ptr;


public final class DataRecCOp
		extends RecCOp<DataRecOp, DataOp, Ptr<DataOp>>
		implements DataRecOp {

	public DataRecCOp(
			CCode<?> code,
			DataRecOp underlying,
			Ptr<DataRecOp> constant) {
		super(code, underlying, constant);
		assert (constant == null
				|| (constant.getAllocation() instanceof DataRecCDAlloc)) :
					"Wrong constant: " + constant;
	}

	@Override
	public DataRecCOp create(
			CCode<?> code,
			DataRecOp underlying,
			Ptr<DataRecOp> constant) {
		return new DataRecCOp(code, underlying, constant);
	}

	@Override
	protected DataCOp loaded(
			CCode<?> code,
			DataOp underlying,
			Ptr<DataOp> constant) {
		return new DataCOp(code, underlying, constant);
	}

	@Override
	protected DataOp underlyingConstant(CCode<?> code, Ptr<DataOp> constant) {

		final DataCDAlloc alloc = (DataCDAlloc) constant.getAllocation();

		return alloc.getUnderlyingPtr().op(null, code.getUnderlying());
	}

}
