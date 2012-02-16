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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.Fp32recOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public class Fp32rec extends Rec<Fp32recOp, Float> {

	Fp32rec(SubData<?> enclosing, CodeId id, Content<Fp32rec> content) {
		super(enclosing, id, content);
	}

	@Override
	public DataType getDataType() {
		return DataType.FP32;
	}

	@Override
	public final Fp32rec setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final Fp32rec setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final Fp32rec setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateFp32(
				getEnclosing().getAllocation(),
				this,
				getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		writer.writeFp32(getAllocation(), getValue());
	}
}
