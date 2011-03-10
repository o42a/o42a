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

import static org.o42a.codegen.debug.DbgFieldType.DBG_FIELD_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


final class DbgStruct extends Struct<DbgStruct.Op> {

	private final Type<?> type;

	private AnyPtrRec name;
	private Rec<DataOp<Int32op>, Integer> dataLayout;
	private Rec<DataOp<Int32op>, Integer> size;

	DbgStruct(Type<?> type) {
		this.type = type;
	}

	public final Type<?> getTarget() {
		return this.type;
	}

	public final AnyPtrRec name() {
		return this.name;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("DEBUG").sub("TYPE").sub(this.type.codeId(factory));
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.name = data.addPtr("name");
		this.dataLayout = data.addInt32("layout");
		this.size = data.addInt32("size");

		final Generator generator = data.getGenerator();
		final Debug debug = generator;

		debug.setName(
				this.name,
				generator.id("DEBUG")
				.sub("TYPE_NAME")
				.sub(this.type.codeId(generator)),
				this.type.codeId(generator).getId());
		this.dataLayout.setValue(getTarget().layout(generator).toBinaryForm());
		this.size.setValue(getTarget().size(generator));
		for (Data<?> fieldData : getTarget().iterate(generator)) {

			final DbgFieldType field = data.addInstance(
					fieldData.getId().getLocal(),
					DBG_FIELD_TYPE);

			field.fill(this, fieldData);
		}
	}

	@Override
	protected void fill() {
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

}
