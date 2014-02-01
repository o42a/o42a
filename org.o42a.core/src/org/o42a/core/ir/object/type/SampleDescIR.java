/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.RelRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.util.string.ID;


public final class SampleDescIR implements Content<SampleDescIR.Type> {

	public static final Type SAMPLE_DESC_IR = new Type();

	private final ObjectIRBody bodyIR;

	public SampleDescIR(ObjectIRBody bodyIR) {
		this.bodyIR = bodyIR;
	}

	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {
		instance.body().setConstant(true).setValue(
				this.bodyIR.pointer(instance.getGenerator()).relativeTo(
						instance.pointer(instance.getGenerator())));
	}

	@Override
	public String toString() {
		return this.bodyIR.toString();
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final RelRecOp body(Code code) {
			return relPtr(null, code, getType().body());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<Op> {

		private RelRec body;

		private Type() {
			super(ID.rawId("o42a_obj_sample_t"));
		}

		public final RelRec body() {
			return this.body;
		}

		@Override
		public final Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.body = data.addRelPtr("body");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0111);
		}

	}

}
