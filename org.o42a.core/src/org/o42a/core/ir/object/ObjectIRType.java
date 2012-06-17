/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.ObjectTypeIR.OBJECT_TYPE_ID;
import static org.o42a.core.ir.object.type.FieldDescIR.FIELD_DESC_IR;
import static org.o42a.core.ir.object.type.OverriderDescIR.OVERRIDER_DESC_IR;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.object.type.FieldDescIR;
import org.o42a.core.ir.object.type.OverriderDescIR;
import org.o42a.core.ir.op.RelList;
import org.o42a.util.string.ID;


public class ObjectIRType extends Type<ObjectIRType.Op> {

	public static final ObjectIRType OBJECT_TYPE = new ObjectIRType();

	private static final ID DATA_ID = ID.id("data");
	private static final ID FIELD_PREFIX_ID = ID.id("field");
	private static final ID OVERRIDER_PREFIX_ID = ID.id("overrider");

	private ObjectIRData data;
	private RelList<FieldDescIR> fields;
	private RelList<OverriderDescIR> overriders;
	private Int32rec mainBodyLayout;

	private ObjectIRType() {
		super(ID.rawId("o42a_obj_stype_t"));
	}

	public final ObjectIRData data() {
		return this.data;
	}

	public final RelList<FieldDescIR> fields() {
		return this.fields;
	}

	public final RelList<OverriderDescIR> overriders() {
		return this.overriders;
	}

	public final Int32rec mainBodyLayout() {
		return this.mainBodyLayout;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.data = data.addInstance(DATA_ID, OBJECT_DATA_TYPE);
		this.fields = new Fields().allocate(data, "fields");
		this.overriders = new Overriders().allocate(data, "overriders");
		this.mainBodyLayout = data.addInt32("main_body_layout");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0101);
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final ObjectIRType getType() {
			return (ObjectIRType) super.getType();
		}

		public final ObjectIRData.Op data(Code code) {
			return struct(null, code, getType().data());
		}

		public final ObjectTypeOp op(
				CodeBuilder builder,
				ObjectPrecision precision) {
			return new ObjectTypeOp(builder, this, precision);
		}

		@Override
		protected ID fieldId(Code code, ID local) {
			return OBJECT_TYPE_ID.setLocal(local);
		}

	}

	private static final class Fields extends RelList<FieldDescIR> {


		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				FieldDescIR item) {

			final FldIR fld = item.fld();
			final ID id = FIELD_PREFIX_ID.detail(fld.getId().getLocal());
			final FieldDescIR.Type desc =
					data.addInstance(id, FIELD_DESC_IR, item);

			return desc.data(data.getGenerator()).getPointer();
		}

	}

	private static final class Overriders extends RelList<OverriderDescIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				OverriderDescIR item) {

			final Fld fld = item.fld();
			final ID id = OVERRIDER_PREFIX_ID.detail(fld.getId());
			final OverriderDescIR.Type desc =
					data.addInstance(id, OVERRIDER_DESC_IR, item);

			return desc.data(data.getGenerator()).getPointer();
		}

	}

}
