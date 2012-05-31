/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.field.object.FldCtrFinishFunc.FLD_CTR_FINISH;
import static org.o42a.core.ir.field.object.FldCtrStartFunc.FLD_CTR_START;
import static org.o42a.core.ir.system.ThreadSystemType.THREAD_SYSTEM_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldOp;


public final class FldCtrOp extends StructOp<FldCtrOp> {

	public static final Type FLD_CTR_TYPE = new Type();

	private FldCtrOp(StructWriter<FldCtrOp> writer) {
		super(writer);
	}

	public BoolOp start(Code code, FldOp fld) {

		final FuncPtr<FldCtrStartFunc> fn =
				code.getGenerator().externalFunction().link(
						"o42a_fld_start",
						FLD_CTR_START);

		ptr(null, code, FLD_CTR_TYPE.fld())
		.store(code, fld.toData(null, code));

		return fn.op(null, code)
				.call(code, fld.host().objectType(code).ptr().data(code), this);
	}

	public void finish(Code code, FldOp fld) {

		final FuncPtr<FldCtrFinishFunc> fn =
				code.getGenerator().externalFunction().link(
						"o42a_fld_finish",
						FLD_CTR_FINISH);

		fn.op(null, code)
		.call(code, fld.host().objectType(code).ptr().data(code), this);
	}

	public static final class Type
			extends org.o42a.codegen.data.Type<FldCtrOp> {

		private StructRec<FldCtrOp> prev;
		private StructRec<FldCtrOp> next;
		private DataRec fld;
		private SystemData thread;

		private Type() {
		}

		public final StructRec<FldCtrOp> prev() {
			return this.prev;
		}

		public final StructRec<FldCtrOp> next() {
			return this.next;
		}

		public final DataRec fld() {
			return this.fld;
		}

		public final SystemData thread() {
			return this.thread;
		}

		@Override
		public final FldCtrOp op(StructWriter<FldCtrOp> writer) {
			return new FldCtrOp(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.rawId("o42a_fld_ctr_t");
		}

		@Override
		protected void allocate(SubData<FldCtrOp> data) {
			this.prev = data.addPtr("prev", FLD_CTR_TYPE);
			this.next = data.addPtr("next", FLD_CTR_TYPE);
			this.fld = data.addDataPtr("fld");
			this.thread = data.addSystem("thread", THREAD_SYSTEM_TYPE);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a02ff);
		}

	}

}
