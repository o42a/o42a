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
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public final class RelRec extends Rec<RelRecOp, RelPtr> {

	RelRec(SubData<?> enclosing, CodeId id) {
		super(enclosing, id);
	}

	@Override
	public final DataType getDataType() {
		return DataType.REL_PTR;
	}

	@Override
	public final RelRec setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final RelRec setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final RelRec setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	public final void setNull() {
		setValue(getPointer().relativeTo(getPointer()));
	}

	@Override
	public RelRecOp fieldOf(CodeId id, Code code, StructOp<?> struct) {
		return struct.relPtr(id, code, this);
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateRelPtr(
				getEnclosing().getAllocation(),
				this,
				getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		getValue().get().getAllocation().write(writer, getAllocation());
	}

}
