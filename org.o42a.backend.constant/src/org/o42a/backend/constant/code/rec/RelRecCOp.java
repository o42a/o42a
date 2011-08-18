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
import org.o42a.backend.constant.code.op.RelCOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.RelRecOp;


public final class RelRecCOp
		extends RecCOp<RelRecOp, RelOp>
		implements RelRecOp {

	public RelRecCOp(CCode<?> code, RelRecOp underlying) {
		super(code, underlying);
	}

	@Override
	public final RelRecCOp create(CCode<?> code, RelRecOp underlying) {
		return new RelRecCOp(code, underlying);
	}

	@Override
	protected final RelCOp loaded(CCode<?> code, RelOp underlying) {
		return new RelCOp(code, underlying);
	}

}
