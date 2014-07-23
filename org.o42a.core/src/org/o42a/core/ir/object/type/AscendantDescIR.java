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

import static org.o42a.core.ir.object.ObjectIRDesc.OBJECT_DESC_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRDescOp;
import org.o42a.util.string.ID;


public final class AscendantDescIR implements Content<AscendantDescIR.Type> {

	public static final Type ASCENDANT_DESC_IR = new Type();

	private final ObjectIRBody bodyIR;

	public AscendantDescIR(ObjectIRBody bodyIR) {
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

		final Generator generator = instance.getGenerator();
		final ObjectIR ascendantIR =
				this.bodyIR.getSampleDeclaration().ir(generator);

		instance.desc()
		.setConstant(true)
		.setValue(ascendantIR.getDataIR().getDescPtr());
		instance.body()
		.setConstant(true)
		.setValue(
				this.bodyIR.data(generator)
				.getPointer()
				.relativeTo(instance.pointer(generator)));
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

		public final StructRecOp<ObjectIRDescOp> desc(Code code) {
			return ptr(null, code, getType().desc());
		}

		public final RelRecOp body(Code code) {
			return relPtr(null, code, getType().body());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<Op> {

		private StructRec<ObjectIRDescOp> desc;
		private RelRec body;

		private Type() {
			super(ID.rawId("o42a_obj_ascendant_t"));
		}

		public final StructRec<ObjectIRDescOp> desc() {
			return this.desc;
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
			this.desc = data.addPtr("desc", OBJECT_DESC_TYPE);
			this.body = data.addRelPtr("body");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0110);
		}

	}

}
