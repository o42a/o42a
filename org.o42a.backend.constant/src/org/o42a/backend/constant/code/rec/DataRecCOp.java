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
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;


public final class DataRecCOp
		extends RecCOp<DataRecOp, DataOp>
		implements DataRecOp {

	public DataRecCOp(CCode<?> code, DataRecOp underlying) {
		super(code, underlying);
	}

	@Override
	public DataRecCOp create(CCode<?> code, DataRecOp underlying) {
		return new DataRecCOp(code, underlying);
	}

	@Override
	protected DataCOp loaded(CCode<?> code, DataOp underlying) {
		return new DataCOp(code, underlying);
	}

}
