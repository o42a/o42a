/*
    Compiler Code Generator
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.codegen.data;

import static org.o42a.util.fn.Holder.holder;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.Int16recOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public final class Int16rec extends Rec<Int16recOp, Short> {

	Int16rec(SubData<?> enclosing, CodeId id) {
		super(enclosing, id);
	}

	@Override
	public DataType getDataType() {
		return DataType.INT16;
	}

	@Override
	public final Int16rec setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final Int16rec setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final Int16rec setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	public final Int16rec setValue(short value) {
		setValue(holder(value));
		return this;
	}

	@Override
	public Int16recOp fieldOf(CodeId id, Code code, StructOp<?> struct) {
		return struct.int16(id, code, this);
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateInt16(
				getEnclosing().getAllocation(),
				this,
				getPointer().getProtoAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		writer.writeInt16(getPointer().getAllocation(), getValue());
	}

}
