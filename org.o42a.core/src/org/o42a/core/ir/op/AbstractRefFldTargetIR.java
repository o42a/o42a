/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import static org.o42a.core.ir.object.state.DepIR.DEP_IR;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.state.DepIR.Op;
import org.o42a.core.ir.object.state.DepIR.Type;
import org.o42a.util.string.ID;


public abstract class AbstractRefFldTargetIR implements RefTargetIR {

	private Type instance;

	public final Type getInstance() {
		return this.instance;
	}

	@Override
	public final Data<?> allocate(ID id, SubData<?> data) {
		this.instance = data.addInstance(id, DEP_IR);
		this.instance.object().setNull();
		return this.instance.data(data.getGenerator());
	}

	@Override
	public final AbstractRefFldTargetOp op(Code code, StructOp<?> data) {

		final Op ptr = data.struct(null, code, getInstance());

		return createOp(ptr);
	}

	protected abstract AbstractRefFldTargetOp createOp(Op ptr);

}
