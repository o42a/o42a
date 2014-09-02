/*
    Compiler Core
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
package org.o42a.core.ir.object.type;

import static org.o42a.core.ir.object.op.ByObjectFunc.BY_OBJECT;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyRec;
import org.o42a.codegen.data.FuncRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.op.ByObjectFunc;
import org.o42a.util.string.ID;


public class ValueTypeDescOp extends StructOp<ValueTypeDescOp> {

	public static final Type VALUE_TYPE_DESC_TYPE = new Type();

	private ValueTypeDescOp(StructWriter<ValueTypeDescOp> writer) {
		super(writer);
	}

	public static final class Type
			extends org.o42a.codegen.data.Type<ValueTypeDescOp> {

		private AnyRec name;
		private FuncRec<ByObjectFunc> mark;
		private FuncRec<ByObjectFunc> sweep;

		private Type() {
			super(ID.rawId("o42a_val_type_t"));
		}

		public final AnyRec name() {
			return this.name;
		}

		public final FuncRec<ByObjectFunc> mark() {
			return this.mark;
		}

		public final FuncRec<ByObjectFunc> sweep() {
			return this.sweep;
		}

		@Override
		public ValueTypeDescOp op(StructWriter<ValueTypeDescOp> writer) {
			return new ValueTypeDescOp(writer);
		}

		@Override
		protected void allocate(SubData<ValueTypeDescOp> data) {
			this.name = data.addPtr("name");
			this.mark = data.addFuncPtr("mark", BY_OBJECT);
			this.sweep = data.addFuncPtr("sweep", BY_OBJECT);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0003);
		}

	}

}
