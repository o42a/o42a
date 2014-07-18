/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.util.string.ID;


public final class VmtIRChain extends Type<VmtIRChain.Op> {

	public static final VmtIRChain VMT_IR_CHAIN_TYPE = new VmtIRChain();

	private DataRec vmt;
	private StructRec<Op> prev;

	private VmtIRChain() {
		super(ID.rawId("o42a_obj_vmtc_t"));
	}

	public final DataRec vmt() {
		return this.vmt;
	}

	public final StructRec<Op> prev() {
		return this.prev;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.vmt = data.addDataPtr("vmt");
		this.prev = data.addPtr("prev", VMT_IR_CHAIN_TYPE);
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0102);
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final VmtIRChain getType() {
			return (VmtIRChain) super.getType();
		}

		public final DataRecOp vmt(ID id, Code code) {
			return ptr(id, code, getType().vmt());
		}

		public final StructRecOp<Op> prev(ID id, Code code) {
			return ptr(id, code, getType().prev());
		}

	}

}
