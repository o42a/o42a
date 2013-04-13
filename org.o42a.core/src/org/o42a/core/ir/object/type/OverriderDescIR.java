/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.type.FieldDescIR.FIELD_DESC_IR;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRTypeOp;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public final class OverriderDescIR implements Content<OverriderDescIR.Type> {

	public static final Type OVERRIDER_DESC_IR = new Type();

	private final Fld<?> fld;

	public OverriderDescIR(Fld<?> fld) {
		this.fld = fld;
	}

	public final Fld<?> fld() {
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
				declaredInIR.getTypeIR().fieldDescIR(
						this.fld.getKey());
		final Obj definedIn = this.fld.getDefinedIn();
		final ObjectIR definedInIR = definedIn.ir(this.fld.getGenerator());

		instance.field().setConstant(true).setValue(
				fieldDescIR.getInstance().pointer(generator));
		instance.definedIn().setConstant(true).setValue(
				definedInIR.getTypeIR().getObjectType().pointer(generator));
		instance.body().setConstant(true).setValue(
				this.fld.getBodyIR().pointer(generator).relativeTo(
						instance.pointer(generator)));
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

		public final StructRecOp<FieldDescIR.Op> field(Code code) {
			return ptr(null, code, getType().field());
		}

		public final RelRecOp fld(Code code) {
			return relPtr(null, code, getType().body());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<Op> {

		private StructRec<FieldDescIR.Op> field;
		private StructRec<ObjectIRTypeOp> definedIn;
		private RelRec body;

		private Type() {
			super(ID.rawId("o42a_obj_overrider_t"));
		}

		public final StructRec<FieldDescIR.Op> field() {
			return this.field;
		}

		public final StructRec<ObjectIRTypeOp> definedIn() {
			return this.definedIn;
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
			this.field = data.addPtr("field", FIELD_DESC_IR);
			this.definedIn = data.addPtr("defined_in", OBJECT_TYPE);
			this.body = data.addRelPtr("body");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0113);
		}

	}

}
