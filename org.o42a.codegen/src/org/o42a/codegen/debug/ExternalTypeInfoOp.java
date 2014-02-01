/*
    Compiler Code Generator
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyRec;
import org.o42a.codegen.data.Int32rec;
import org.o42a.codegen.data.SubData;


final class ExternalTypeInfoOp extends StructOp<ExternalTypeInfoOp> {

	static final Type EXTERNAL_TYPE_INFO = new Type();

	private ExternalTypeInfoOp(StructWriter<ExternalTypeInfoOp> writer) {
		super(writer);
	}

	static final class Type
			extends org.o42a.codegen.data.Type<ExternalTypeInfoOp> {

		private Int32rec typeCode;
		private Int32rec fieldNum;
		private AnyRec name;

		private Type() {
			super(DEBUG_ID.sub("TypeInfo"));
		}

		@Override
		public boolean isDebugInfo() {
			return true;
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
		public ExternalTypeInfoOp op(StructWriter<ExternalTypeInfoOp> writer) {
			return new ExternalTypeInfoOp(writer);
		}

		@Override
		protected void allocate(SubData<ExternalTypeInfoOp> data) {
			this.typeCode = data.addInt32("type_code");
			this.fieldNum = data.addInt32("field_num");
			this.name = data.addPtr("name");
		}

	}

}
