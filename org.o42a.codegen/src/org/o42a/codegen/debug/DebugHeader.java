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


public class DebugHeader implements Content<DebugHeader.HeaderType> {

	public static final HeaderType DEBUG_HEADER_TYPE = new HeaderType();

	public static final int DBG_HDR_STATIC = 0x01;
	public static final int DBG_HDR_STACK = 0x02;
	public static final int DBG_HDR_GLOBAL = 0x02;
	public static final int DBG_HDR_COMMENT = 0x04;

	private final Type<?> target;

	public DebugHeader(Type<?> target) {
		this.target = target;
	}

	public final Type<?> getTarget() {
		return this.target;
	}

	@Override
	public void allocated(HeaderType instance) {
	}

	@Override
	public void fill(HeaderType instance) {

		final Generator generator = instance.getGenerator();
		final Debug debug = generator;
		final Data<Op> data = instance.data(generator);
		final Type<?> enclosing = data.getEnclosing();

		if (enclosing == null) {

			final Global<?, ?> global = data.getGlobal();

			instance.flags().setValue(DBG_HDR_STATIC | DBG_HDR_GLOBAL);
			debug.setName(
					instance.name(),
					generator.id("DEBUG").sub("global_name")
					.sub(global.getId()),
					global.getId().toString());
			instance.enclosing().setNull();
		} else {

			final CodeId fieldName = DebugFieldInfo.fieldName(data);

			instance.flags().setValue(0);
			debug.setName(
					instance.name(),
					generator.id("DEBUG").sub("field_name").sub(fieldName),
					fieldName.toString());
			instance.enclosing().setValue(
					enclosing.pointer(generator).relativeTo(
							data.getPointer()));
		}

		final DebugTypeInfo typeInfo = debug.typeInfo(instance);

		instance.typeCode().setValue(typeInfo.getCode());
		instance.typeInfo().setValue(typeInfo.pointer(generator).toAny());
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

	public static final class HeaderType extends Type<Op> {

		private Int32rec flags;
		private Int32rec typeCode;
		private AnyPtrRec name;
		private AnyPtrRec typeInfo;
		private RelPtrRec enclosing;

		private HeaderType() {
		}

		@Override
		public boolean isPacked() {
			return true;
		}

		@Override
		public boolean isDebugInfo() {
			return true;
		}

		public final Int32rec flags() {
			return this.flags;
		}

		public final Int32rec typeCode() {
			return this.typeCode;
		}

		public final AnyPtrRec name() {
			return this.name;
		}

		public final AnyPtrRec typeInfo() {
			return this.typeInfo;
		}

		public final RelPtrRec enclosing() {
			return this.enclosing;
		}

		@Override
		public Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("Header");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.flags = data.addInt32("flags");
			this.typeCode = data.addInt32("type_code");
			this.enclosing = data.addRelPtr("enclosing");
			this.name = data.addPtr("name");
			this.typeInfo = data.addPtr("type_info");
		}

	}

}
