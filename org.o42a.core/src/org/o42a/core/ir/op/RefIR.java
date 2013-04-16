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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathIR;
import org.o42a.util.string.ID;


public final class RefIR extends PathIR implements RefTargetIR {

	private final Generator generator;
	private final Ref ref;
	private RefTargetIR targetIR;

	public RefIR(Generator generator, Ref ref) {
		this.generator = generator;
		this.ref = ref;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Ref ref() {
		return this.ref;
	}

	@Override
	public final Data<?> allocate(ID id, SubData<?> data) {
		return targetIR().allocate(id, data);
	}

	@Override
	public final RefIROp op(Code code, StructOp<?> data) {
		return new RefIROp(this, targetIR().op(code, data));
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

	final RefTargetIR targetIR() {
		if (this.targetIR != null) {
			return this.targetIR;
		}
		return this.targetIR = stepTargetIR(this, ref().getPath().lastStep());
	}

}
