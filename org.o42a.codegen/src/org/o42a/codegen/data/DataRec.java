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
package org.o42a.codegen.data;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


public final class DataRec extends PtrRec<DataRecOp, Ptr<DataOp>> {

	DataRec(SubData<?> enclosing, ID id) {
		super(enclosing, id);
	}

	@Override
	public final DataType getDataType() {
		return DataType.DATA_PTR;
	}

	@Override
	public final DataRec setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final DataRec setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final DataRec setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	@Override
	public final void setNull() {
		setValue(getGenerator().getGlobals().nullDataPtr());
	}

	@Override
	public DataRecOp fieldOf(ID id, Code code, StructOp<?> struct) {
		return struct.ptr(id, code, this);
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateDataPtr(
				getEnclosing().getAllocation(),
				this,
				getPointer().getProtoAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		getValue().get().getAllocation().write(writer, getAllocation());
	}

}
