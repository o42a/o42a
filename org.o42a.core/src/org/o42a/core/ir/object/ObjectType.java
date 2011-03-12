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
import static org.o42a.core.ir.object.ObjectDataType.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.OverriderDescIR.OVERRIDER_DESC_IR;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.op.RelList;


public class ObjectType extends Type<ObjectType.Op> {

	public static final ObjectType OBJECT_TYPE = new ObjectType();

	private ObjectDataType objectData;
	private RelList<FieldDescIR> fields;
	private RelList<OverriderDescIR> overriders;
	private Int32rec mainBodyLayout;

	private ObjectType() {
	}

	public final ObjectDataType objectData() {
		return this.objectData;
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
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("ObjectType");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.objectData = data.addInstance(
				data.getGenerator().id("object_data"),
				OBJECT_DATA_TYPE);
		this.fields = new Fields().allocate(data, "fields");
		this.overriders = new Overriders().allocate(data, "overriders");
		this.mainBodyLayout = data.addInt32("main_body_layout");
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final ObjectType getType() {
			return (ObjectType) super.getType();
		}

		public final ObjectDataType.Op objectData(Code code) {
			return writer().struct(code, getType().objectData());
		}

		public final ObjectTypeOp op(
				CodeBuilder builder,
				ObjectPrecision precision) {
			return new ObjectTypeOp(builder, this, precision);
		}

	}

	private static final class Fields extends RelList<FieldDescIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				FieldDescIR item) {

			final Fld fld = item.fld();
			final Generator generator = fld.getGenerator();
			final CodeId id =
				generator.id("field")
				.detail(fld.getField().ir(generator).getId().getLocal());
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
			final Generator generator = fld.getGenerator();
			final CodeId id =
				generator.id("overrider")
				.detail(fld.getField().ir(generator).getId());
			final OverriderDescIR.Type desc =
				data.addInstance(id, OVERRIDER_DESC_IR, item);

			return desc.data(data.getGenerator()).getPointer();
		}

	}

}
