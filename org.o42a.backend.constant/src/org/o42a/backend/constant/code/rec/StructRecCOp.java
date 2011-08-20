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
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Type;


public class StructRecCOp<S extends StructOp<S>>
		extends RecCOp<StructRecOp<S>, S>
		implements StructRecOp<S> {

	private final Type<S> type;

	public StructRecCOp(
			CCode<?> code,
			StructRecOp<S> underlying,
			Type<S> type) {
		super(code, underlying);
		this.type = type;
	}

	public final Type<S> getType() {
		return this.type;
	}

	@Override
	public StructRecCOp<S> create(CCode<?> code, StructRecOp<S> underlying) {
		return new StructRecCOp<S>(code, underlying, getType());
	}

	@Override
	protected S loaded(CCode<?> code, S underlying) {
		return getType().op(new CStruct<S>(code, underlying));
	}

}
