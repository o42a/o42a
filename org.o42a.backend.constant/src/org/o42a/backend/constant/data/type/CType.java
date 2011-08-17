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
package org.o42a.backend.constant.data.type;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public class CType<S extends StructOp<S>> extends Type<S> {

	private final Type<S> underlying;

	public CType(Type<S> underlying) {
		this.underlying = underlying;
	}

	public final Type<S> getUnderlying() {
		return this.underlying;
	}

	@Override
	public S op(StructWriter<S> writer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void allocate(SubData<S> data) {
		// TODO Auto-generated method stub

	}

}
