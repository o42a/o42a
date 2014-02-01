/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AllocPtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.codegen.data.Ptr;


public abstract class AllocPtrCOp<P extends AllocPtrOp<P>>
		extends DataPtrCOp<P>
		implements AllocPtrOp<P> {

	public AllocPtrCOp(OpBE<P> backend, AllocPlace allocPlace) {
		super(backend, allocPlace);
	}

	public AllocPtrCOp(
			OpBE<P> backend,
			AllocPlace allocPlace,
			Ptr<P> constant) {
		super(backend, allocPlace, constant);
	}

	@Override
	public void allocated(Code code, StructOp<?> enclosing) {
	}

}
