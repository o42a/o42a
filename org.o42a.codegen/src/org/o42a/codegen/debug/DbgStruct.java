/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


final class DbgStruct extends Struct<DbgStruct.Op> {

	private final Debug debug;
	private final Type<?> type;

	private AnyPtrRec name;
	private Rec<DataOp<Int32op>, Integer> dataLayout;
	private Rec<DataOp<Int32op>, Integer> size;

	DbgStruct(Debug debug, Type<?> type) {
		super("DEBUG.TYPE." + type.getId());
		this.debug = debug;
		this.type = type;
	}

	public final Type<?> getType() {
		return this.type;
	}

	public final AnyPtrRec getName() {
		return this.name;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.name = data.addPtr("name");
		this.dataLayout = data.addInt32("layout");
		this.size = data.addInt32("size");

		this.debug.setName(
				this.name,
				"DEBUG.TYPE_NAME." + this.type.getId(),
				this.type.getId());
		this.dataLayout.setValue(getType().getLayout().toBinaryForm());
		this.size.setValue(getType().size());
		for (Data<?> fieldData : getType()) {

			final DbgFieldType field =
				data.addInstance(fieldData.getName(), this.debug.dbgFieldType());

			field.fill(this.debug, this, fieldData);
		}
	}

	@Override
	protected void fill() {
	}

	final Debug debug() {
		return this.debug;
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
