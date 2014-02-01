/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


public final class StructRec<S extends StructOp<S>>
		extends PtrRec<StructRecOp<S>, Ptr<S>> {

	private final Type<S> type;

	StructRec(SubData<?> enclosing, ID id, Type<S> type) {
		super(enclosing, id);
		this.type = type;
	}

	@Override
	public final DataType getDataType() {
		return DataType.DATA_PTR;
	}

	public final Type<S> getType() {
		return this.type;
	}

	@Override
	public final StructRec<S> setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final StructRec<S> setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final StructRec<S> setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	@Override
	public final void setNull() {
		setValue(getGenerator().getGlobals().nullPtr(getType()));
	}

	@Override
	public StructRecOp<S> fieldOf(ID id, Code code, StructOp<?> struct) {
		return struct.ptr(id, code, this);
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocatePtr(
				getEnclosing().getAllocation(),
				this,
				getPointer().getProtoAllocation(),
				this.type.pointer(getGenerator()).getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		assert getValue() != null :
			this + " has no value";
		getValue().get().getAllocation().write(writer, getAllocation());
	}

}
