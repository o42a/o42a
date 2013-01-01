/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.code.op.FpOp;
import org.o42a.util.string.ID;


public abstract class FpCOp<U extends FpOp<U>, T extends Number>
		extends NumCOp<U, T>
		implements FpOp<U> {

	public FpCOp(OpBE<U> backend) {
		super(backend);
	}

	public FpCOp(ID id, CCode<?> code, T constant) {
		super(id, code, constant);
	}

	public FpCOp(OpBE<U> backend, T constant) {
		super(backend, constant);
	}

}
