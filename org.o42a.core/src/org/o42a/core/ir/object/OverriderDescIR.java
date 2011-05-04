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

import static org.o42a.core.ir.object.FieldDescIR.FIELD_DESC_IR;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.Fld;


public final class OverriderDescIR implements Content<OverriderDescIR.Type> {

	public static final Type OVERRIDER_DESC_IR = new Type();

	private final Fld fld;

	OverriderDescIR(Fld fld) {
		this.fld = fld;
	}

	public final Fld fld() {
		return this.fld;
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {

		final Generator generator = instance.getGenerator();
		final ObjectIR declaredInIR =
			this.fld.getDeclaredIn().ir(this.fld.getGenerator());
		final FieldDescIR fieldDescIR =
			declaredInIR.getTypeIR().fieldDescIR(this.fld.getField().getKey());
		final Obj definedIn =
			this.fld.getField().getDefinedIn().getContainer().toObject();
		final ObjectIR definedInIR = definedIn.ir(this.fld.getGenerator());

		instance.field().setValue(
				fieldDescIR.getInstance().pointer(generator));
		instance.definedIn().setValue(
				definedInIR.getTypeIR().getObjectType().pointer(generator));
		instance.body().setValue(
				this.fld.getBodyIR().pointer(generator).relativeTo(
						instance.pointer(generator)));
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

		public final RecOp<FieldDescIR.Op> field(Code code) {
			return ptr(null, code, getType().field());
		}

		public final RecOp<RelOp> fld(Code code) {
			return relPtr(null, code, getType().body());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<OverriderDescIR.Op> {

		private StructRec<FieldDescIR.Op> field;
		private StructRec<ObjectIRType.Op> definedIn;
		private RelPtrRec body;

		private Type() {
		}

		public final StructRec<FieldDescIR.Op> field() {
			return this.field;
		}

		public final StructRec<ObjectIRType.Op> definedIn() {
			return this.definedIn;
		}

		public final RelPtrRec body() {
			return this.body;
		}

		@Override
		public final Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("OverriderDesc");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.field = data.addPtr("field", FIELD_DESC_IR);
			this.definedIn = data.addPtr("defined_in", OBJECT_TYPE);
			this.body = data.addRelPtr("body");
		}

	}

}
