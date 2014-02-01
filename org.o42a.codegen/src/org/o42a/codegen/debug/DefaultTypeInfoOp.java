/*
    Compiler Code Generator
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.codegen.debug.Debug.DEBUG_ID;
import static org.o42a.codegen.debug.DebugFieldInfo.DEBUG_FIELD_INFO_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


final class DefaultTypeInfoOp extends StructOp<DefaultTypeInfoOp> {

	private static final ID TYPE_ID = DEBUG_ID.sub("type");
	private static final ID TYPE_NAME_ID = DEBUG_ID.sub("type_name");
	private static final ID FIELD_ID = ID.id("field");

	private DefaultTypeInfoOp(StructWriter<DefaultTypeInfoOp> writer) {
		super(writer);
	}

	static final class Struct
			extends org.o42a.codegen.data.Struct<DefaultTypeInfoOp> {

		private Int32rec typeCode;
		private Int32rec fieldNum;
		private AnyRec name;
		private final DebugTypeInfo typeInfo;

		Struct(DebugTypeInfo typeInfo) {
			super(TYPE_ID.sub(typeInfo.getType().getId()));
			this.typeInfo = typeInfo;
		}

		@Override
		public boolean isDebugInfo() {
			return true;
		}

		public final Type<?> getTargetType() {
			return this.typeInfo.getType();
		}

		public final int getCode() {
			return this.typeInfo.getCode();
		}

		public final Int32rec typeCode() {
			return this.typeCode;
		}

		public final Int32rec fieldNum() {
			return this.fieldNum;
		}

		public final AnyRec name() {
			return this.name;
		}

		@Override
		public DefaultTypeInfoOp op(StructWriter<DefaultTypeInfoOp> writer) {
			return new DefaultTypeInfoOp(writer);
		}

		@Override
		protected void allocate(SubData<DefaultTypeInfoOp> data) {
			this.typeCode = data.addInt32("type_code");
			this.fieldNum = data.addInt32("field_num");
			this.name = data.addPtr("name");

			final Generator generator = data.getGenerator();
			final Debug debug = generator.getDebug();

			typeCode().setValue(getCode());

			final ID typeId = getTargetType().getId();

			debug.setName(name(), TYPE_NAME_ID.sub(typeId), typeId);

			int fieldNum = 0;

			for (Data<?> field : getTargetType().iterate(generator)) {
				if (addFieldInfo(data, field)) {
					++fieldNum;
				}
			}

			fieldNum().setValue(fieldNum);
		}

		private boolean addFieldInfo(
				SubData<DefaultTypeInfoOp> data,
				Data<?> field) {

			final Type<?> fieldInstance = field.getInstance();

			if (fieldInstance != null) {
				if (fieldInstance.isDebugInfo()) {
					return false;
				}
			}

			data.addInstance(
					FIELD_ID.detail(field.getId()),
					DEBUG_FIELD_INFO_TYPE,
					new DebugFieldInfo(field));

			return true;
		}

		@Override
		protected void fill() {
		}

	}

}
