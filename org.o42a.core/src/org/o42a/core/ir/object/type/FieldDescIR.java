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
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRDescOp;
import org.o42a.util.string.ID;


public final class FieldDescIR implements Content<FieldDescIR.Type> {

	public static final Type FIELD_DESC_IR = new Type();

	private final FldIR fld;
	private Type instance;

	public FieldDescIR(FldIR fld) {
		this.fld = fld;
	}

	public final FldIR fld() {
		return this.fld;
	}

	public final Type getInstance() {
		return this.instance;
	}

	@Override
	public void allocated(Type instance) {
		this.instance = instance;
	}

	@Override
	public void fill(Type instance) {

		final Generator generator = instance.getGenerator();
		final ObjectIR declaredInIR =
				this.fld.getDeclaredIn()
				.type()
				.getSampleDeclaration()
				.ir(generator);

		instance.declaredIn()
		.setConstant(true)
		.setValue(
				declaredInIR.getDataIR()
				.getDesc()
				.data(generator)
				.getPointer());
		instance.kind()
		.setConstant(true)
		.setValue(this.fld.getKind().code());
		instance.fld()
		.setConstant(true)
		.setValue(
				this.fld.data(generator)
				.getPointer()
				.relativeTo(
						this.fld.getBodyIR()
						.data(generator)
						.getPointer()));
	}

	@Override
	public String toString() {
		return this.fld.toString();
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final StructRecOp<ObjectIRDescOp> declaredIn(Code code) {
			return ptr(null, code, getType().declaredIn());
		}

		public final Int32recOp kind(Code code) {
			return int32(null, code, getType().kind());
		}

		public final RelRecOp fld(Code code) {
			return relPtr(null, code, getType().fld());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<Op> {

		private StructRec<ObjectIRDescOp> declaredIn;
		private Int32rec kind;
		private RelRec fld;

		private Type() {
			super(ID.rawId("o42a_obj_field_t"));
		}

		public final StructRec<ObjectIRDescOp> declaredIn() {
			return this.declaredIn;
		}

		public final Int32rec kind() {
			return this.kind;
		}

		public final RelRec fld() {
			return this.fld;
		}

		@Override
		public final Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.declaredIn = data.addPtr("declared_in", OBJECT_DESC_TYPE);
			this.kind = data.addInt32("kind");
			this.fld = data.addRelPtr("fld");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0112);
		}

	}

}
