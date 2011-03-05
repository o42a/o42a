/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectType.OBJECT_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.field.Fld;


public final class FieldDescIR implements Content<FieldDescIR.Type> {

	public static final Type FIELD_DESC_IR = new Type();

	private final Fld fld;
	private Type instance;

	FieldDescIR(Fld fld) {
		this.fld = fld;
	}

	public final Fld fld() {
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

		final Generator generator = instance.generator();
		final ObjectIR declaredInIR =
			this.fld.getDeclaredIn().ir(fld().getGenerator());

		instance.getDeclaredIn().setValue(
				declaredInIR.getTypeIR().getObjectType()
				.data(generator).getPointer());
		instance.getKind().setValue(this.fld.getKind().getCode());
		instance.getFld().setValue(
				this.fld.getInstance().data(generator).getPointer()
				.relativeTo(
						this.fld.getBodyIR().data(generator).getPointer()));
	}

	@Override
	public String toString() {
		return this.fld.toString();
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataOp<ObjectType.Op> declaredIn(Code code) {
			return writer().ptr(code, getType().getDeclaredIn());
		}

		public final DataOp<Int32op> kind(Code code) {
			return writer().int32(code, getType().getKind());
		}

		public final DataOp<RelOp> fld(Code code) {
			return writer().relPtr(code, getType().getFld());
		}

		@Override
		public final Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<FieldDescIR.Op> {

		private StructPtrRec<ObjectType.Op> declaredIn;
		private Int32rec kind;
		private RelPtrRec fld;

		private Type() {
		}

		public final StructPtrRec<ObjectType.Op> getDeclaredIn() {
			return this.declaredIn;
		}

		public final Int32rec getKind() {
			return this.kind;
		}

		public final RelPtrRec getFld() {
			return this.fld;
		}

		@Override
		public final Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("FieldDesc");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.declaredIn = data.addPtr("declared_in", OBJECT_TYPE);
			this.kind = data.addInt32("kind");
			this.fld = data.addRelPtr("fld");
		}

	}

}
