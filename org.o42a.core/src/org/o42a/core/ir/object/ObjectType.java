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

import static org.o42a.core.ir.IRSymbolSeparator.DETAIL;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.op.RelList;


public class ObjectType extends Type<ObjectType.Op> {

	private final ObjectIRGenerator generator;
	private ObjectDataType objectData;
	private RelList<FieldDescIR> fields;
	private RelList<OverriderDescIR> overriders;
	private Int32rec mainBodyLayout;

	ObjectType(ObjectIRGenerator generator) {
		super("ObjectType");
		this.generator = generator;
	}

	public final ObjectDataType getObjectData() {
		return this.objectData;
	}

	public final RelList<FieldDescIR> getFields() {
		return this.fields;
	}

	public final RelList<OverriderDescIR> getOverriders() {
		return this.overriders;
	}

	public final Int32rec getMainBodyLayout() {
		return this.mainBodyLayout;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.objectData =
			data.addInstance("object_data", this.generator.objectDataType());
		this.fields = new Fields().allocate(this.generator, data, "fields");
		this.overriders =
			new Overriders().allocate(this.generator, data, "overriders");
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

		public final ObjectDataType.Op data(Code code) {
			return writer().struct(code, getType().getObjectData());
		}

		public final ObjectDataOp data(
				CodeBuilder builder,
				Code code,
				ObjectPrecision precision) {
			return data(code).op(builder, precision);
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

	private static final class Fields extends RelList<FieldDescIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				FieldDescIR item) {

			final Fld fld = item.fld();
			final IRGenerator generator = fld.getGenerator();
			final String name =
				"field" + DETAIL
				+ fld.getField().ir(generator).getLocalName();
			final FieldDescIR.Type desc =
				data.addInstance(name, generator.fieldDescType(), item);

			return desc.getPointer();
		}

	}

	private static final class Overriders extends RelList<OverriderDescIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				OverriderDescIR item) {

			final Fld fld = item.fld();
			final IRGenerator generator = fld.getGenerator();
			final String name =
				"overrider" + DETAIL
				+ fld.getField().ir(generator).getId();
			final OverriderDescIR.Type desc =
				data.addInstance(name, generator.overriderDescType(), item);

			return desc.getPointer();
		}

	}

}
