/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


final class DbgFieldType extends Type<DbgFieldType.Op> {

	public static final DbgFieldType DBG_FIELD_TYPE = new DbgFieldType();

	private AnyPtrRec name;
	private AnyPtrRec dbgStruct;
	private Rec<DataOp<Int32op>, Integer> dataType;
	private Rec<DataOp<RelOp>, RelPtr> offset;

	private DbgFieldType() {
	}

	public final AnyPtrRec getName() {
		return this.name;
	}

	public final AnyPtrRec getDbgStruct() {
		return this.dbgStruct;
	}

	public final Rec<DataOp<Int32op>, Integer> getDataType() {
		return this.dataType;
	}

	public final Rec<DataOp<RelOp>, RelPtr> getOffset() {
		return this.offset;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("DEBUG").sub("Field");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.name = data.addPtr("name");
		this.dbgStruct = data.addPtr("dbg_struct");
		this.dataType = data.addInt32("data_type");
		this.offset = data.addRelPtr("offset");
	}

	void fill(DbgStruct enclosing, Data<?> fieldData) {

		final Generator generator = fieldData.getGenerator();
		final Debug debug = fieldData.getGenerator();
		final DbgStruct struct = debug.writeStruct(fieldData);

		if (enclosing != null) {
			getOffset().setValue(
					fieldData.getPointer().relativeTo(
							enclosing.getType().data(generator).getPointer()));
		} else {
			getOffset().setValue(
					fieldData.getPointer().relativeTo(
							fieldData.getPointer()));
		}
		if (enclosing == null
				&& struct.getType().codeId(generator).equals(
						fieldData.getId())) {
			getName().setValue(struct.getName().getValue());
		} else {
			debug.setName(
					getName(),
					generator
					.id("DEBUG")
					.sub("FIELD_NAME")
					.sub(fieldData.getId()),
					fieldData.getId().getLocal().getId());
		}
		if (struct != null) {
			getDbgStruct().setValue(
					struct.data(generator).getPointer().toAny());
		} else {
			getDbgStruct().setNull();
		}
		getDataType().setValue(fieldData.getDataType().getCode());
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

}
