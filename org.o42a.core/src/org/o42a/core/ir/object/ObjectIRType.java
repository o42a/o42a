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
import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
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


public class ObjectIRType extends Type<ObjectIRType.Op> {

	public static final ObjectIRType OBJECT_TYPE = new ObjectIRType();

	private ObjectIRData data;
	private RelList<FieldDescIR> fields;
	private RelList<OverriderDescIR> overriders;
	private Int32rec mainBodyLayout;

	private ObjectIRType() {
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
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("ObjectType");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.data = data.addInstance(
				data.getGenerator().id("data"),
				OBJECT_DATA_TYPE);
		this.fields = new Fields().allocate(data, "fields");
		this.overriders = new Overriders().allocate(data, "overriders");
		this.mainBodyLayout = data.addInt32("main_body_layout");
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
		protected CodeId fieldId(Code code, CodeId local) {
			return code.id("object_type").setLocal(local);
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
