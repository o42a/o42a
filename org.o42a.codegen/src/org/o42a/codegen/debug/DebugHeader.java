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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


public class DebugHeader implements Content<DebugHeader.HeaderType> {

	private static final ID FIELD_NAME_ID = DEBUG_ID.sub("field_name");

	private static final ID GLOBAL_NAME_ID = DEBUG_ID.sub("global_name");

	public static final HeaderType DEBUG_HEADER_TYPE = new HeaderType();

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
		final Debug debug = generator.getDebug();
		final Data<?> data = getTarget().data(generator);

		if (data.getEnclosing() == null) {

			final Global<?, ?> global = data.getGlobal();

			debug.setName(
					instance.name(),
					GLOBAL_NAME_ID.sub(global.getId()),
					global.getId());
			instance.enclosing().setNull();
		} else {

			final ID fieldName = data.getId();

			debug.setName(
					instance.name(),
					FIELD_NAME_ID.sub(fieldName),
					fieldName);
			instance.enclosing().setValue(
					data.getEnclosing().pointer(generator).relativeTo(
							data.getPointer()));
		}

		final DebugTypeInfo typeInfo = getTarget().getTypeInfo();

		instance.typeCode().setValue(typeInfo.getCode());
		instance.typeInfo().setValue(typeInfo.getPointer());
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final HeaderType getType() {
			return (HeaderType) super.getType();
		}

		public final Int32recOp typeCode(Code code) {
			return int32(null, code, getType().typeCode());
		}

		public final AnyRecOp name(Code code) {
			return ptr(null, code, getType().name());
		}

		public final AnyRecOp typeInfo(Code code) {
			return ptr(null, code, getType().typeInfo());
		}

		public final RelRecOp enclosing(Code code) {
			return relPtr(null, code, getType().enclosing());
		}

		@Override
		public void allocated(Code code, StructOp<?> enclosing) {
			fillAllocatedHeader(code, enclosing);
			super.allocated(code, enclosing);
		}

		private void fillAllocatedHeader(Code code, StructOp<?> enclosing) {

			final Generator generator = code.getGenerator();
			final Debug debug = generator.getDebug();

			if (enclosing == null) {
				name(code).store(code, code.nullPtr());
				enclosing(code).store(code, code.nullRelPtr());
				typeCode(code).store(code, code.int32(0));
				typeInfo(code).store(code, code.nullPtr());
			} else {

				final ID fieldName = getType().getId();

				name(code).store(
						code,
						debug.allocateName(
								FIELD_NAME_ID.sub(fieldName),
								fieldName).op(null, code));

				final Type<?> enclosingType = enclosing.getType();

				enclosing(code).store(
						code,
						getType()
						.pointer(generator)
						.relativeTo(enclosingType.pointer(generator))
						.op(null, code));

				final DebugTypeInfo typeInfo =
						enclosing.getType().getTypeInfo();

				typeCode(code).store(code, code.int32(typeInfo.getCode()));
				typeInfo(code).store(
						code,
						typeInfo.getPointer().op(null, code));
			}
		}

	}

	public static final class HeaderType extends Type<Op> {

		private Int32rec typeCode;
		private RelRec enclosing;
		private AnyRec name;
		private AnyRec typeInfo;

		private HeaderType() {
			super(DEBUG_ID.sub("Header"));
		}

		@Override
		public TypeAlignment requiredAlignment() {
			return TypeAlignment.PACKED_TYPE;
		}

		@Override
		public boolean isDebugInfo() {
			return true;
		}

		public final Int32rec typeCode() {
			return this.typeCode;
		}

		public final RelRec enclosing() {
			return this.enclosing;
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
		protected void allocate(SubData<Op> data) {
			this.typeCode = data.addInt32("type_code");
			this.enclosing = data.addRelPtr("enclosing");
			this.name = data.addPtr("name");
			this.typeInfo = data.addPtr("type_info");
		}

	}

}
