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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.codegen.data.backend.DataWriter;


public final class Fp64rec extends Rec<DataOp<Fp64op>, Double> {

	Fp64rec(SubData<?> enclosing, CodeId id, Content<Fp64rec> content) {
		super(enclosing, id, content);
	}

	@Override
	public DataType getDataType() {
		return DataType.FP64;
	}

	@Override
	protected void allocate(Generator generator) {
		setAllocation(generator.dataAllocator().allocateFp64(getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		writer.writeFp64(getAllocation(), getValue());
	}

}
