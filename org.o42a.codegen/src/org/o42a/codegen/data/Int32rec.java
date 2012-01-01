/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.Int32recOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public final class Int32rec extends Rec<Int32recOp, Integer> {

	Int32rec(SubData<?> enclosing, CodeId id, Content<Int32rec> content) {
		super(enclosing, id, content);
	}

	@Override
	public DataType getDataType() {
		return DataType.INT32;
	}

	@Override
	public final Int32rec setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateInt32(
				getEnclosing().getAllocation(),
				this,
				getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		writer.writeInt32(getPointer().getAllocation(), getValue());
	}

}
