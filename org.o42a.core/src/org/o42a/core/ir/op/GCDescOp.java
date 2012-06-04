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
package org.o42a.core.ir.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.SubData;


public final class GCDescOp extends StructOp<GCDescOp> {

	public static final Type GC_DESC_TYPE = new Type();

	private GCDescOp(StructWriter<GCDescOp> writer) {
		super(writer);
	}

	public static final class Type
			extends org.o42a.codegen.data.Type<GCDescOp> {

		private Type() {
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		@Override
		public GCDescOp op(StructWriter<GCDescOp> writer) {
			return new GCDescOp(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.rawId("o42a_gc_desc_t");
		}

		@Override
		protected void allocate(SubData<GCDescOp> data) {
			data.addPtr("mark");
			data.addPtr("sweep");
		}

	}

}
