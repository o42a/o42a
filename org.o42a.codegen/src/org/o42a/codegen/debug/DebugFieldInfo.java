/*
    Compiler Code Generator
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.codegen.debug;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public class DebugFieldInfo implements Content<DebugFieldInfo.FieldInfoType> {

	public static final FieldInfoType DEBUG_FIELD_INFO_TYPE =
			new FieldInfoType();

	private final Data<?> fieldData;

	public DebugFieldInfo(Data<?> fieldData) {
		this.fieldData = fieldData;
	}

	public final Data<?> getFieldData() {
		return this.fieldData;
	}

	@Override
	public void allocated(FieldInfoType instance) {
	}

	@Override
	public void fill(FieldInfoType instance) {

		final Data<?> fieldData = getFieldData();
		final Type<?> enclosing = getFieldData().getEnclosing();
		final Generator generator = instance.getGenerator();
		final Debug debug = generator.getDebug();

		instance.dataType().setValue(fieldData.getDataType().getCode());
		instance.offset().setValue(
				fieldData.getPointer().relativeTo(
						enclosing.pointer(generator)));

		final CodeId fieldName = fieldData.getId();

		debug.setName(
				instance.name(),
				generator.id("DEBUG").sub("field_name").sub(fieldName),
				fieldName.toString());

		final DebugTypeInfo typeInfo;

		switch (fieldData.getDataType()) {
		case STRUCT:
			typeInfo = debug.typeInfo(fieldData.getInstance());
			break;
		case DATA_PTR:
			if (fieldData instanceof StructRec) {

				final StructRec<?> structPtr = (StructRec<?>) fieldData;

				typeInfo = debug.typeInfo(structPtr.getType());
			} else {
				typeInfo = null;
			}

			break;
		default:
			typeInfo = null;
		}

		if (typeInfo == null) {
			instance.typeInfo().setNull();
		} else {
			instance.typeInfo().setValue(typeInfo.pointer(generator).toAny());
		}
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

	public static final class FieldInfoType extends Type<Op> {

		private Int32rec dataType;
		private RelRec offset;
		private AnyRec name;
		private AnyRec typeInfo;

		private FieldInfoType() {
		}

		@Override
		public boolean isDebugInfo() {
			return true;
		}

		public final Int32rec dataType() {
			return this.dataType;
		}

		public final RelRec offset() {
			return this.offset;
		}

		public final AnyRec name() {
			return this.name;
		}

		public final AnyRec typeInfo() {
			return this.typeInfo;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("FieldInfo");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.dataType = data.addInt32("data_type");
			this.offset = data.addRelPtr("offset");
			this.name = data.addPtr("name");
			this.typeInfo = data.addPtr("type_info");
		}

	}

}
