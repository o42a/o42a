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
package org.o42a.codegen.data;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.data.backend.DataWriter;


public final class Int32rec extends Rec<DataOp<Int32op>, Integer> {

	Int32rec(String name, String id, Content<Int32rec> content) {
		super(name, id, content);
	}

	@Override
	public DataType getDataType() {
		return DataType.INT32;
	}

	@Override
	protected void allocate(Generator generator) {
		setAllocation(generator.dataAllocator().allocateInt32(
				getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		writer.writeInt32(getPointer().getAllocation(), getValue());
	}

}
