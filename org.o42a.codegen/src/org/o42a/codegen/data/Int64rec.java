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

import static org.o42a.util.func.Holder.holder;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.func.Getter;


public final class Int64rec extends Rec<Int64recOp, Long> {

	private Getter<Ptr<AnyOp>> nativePtr;

	Int64rec(SubData<?> enclosing, CodeId id) {
		super(enclosing, id);
	}

	@Override
	public DataType getDataType() {
		return DataType.INT64;
	}

	@Override
	public final Int64rec setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final Int64rec setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final Int64rec setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	public final Getter<Ptr<AnyOp>> getNativePtr() {
		return this.nativePtr;
	}

	public final void setNativePtr(Getter<Ptr<AnyOp>> nativePtr) {
		this.nativePtr = nativePtr;
		super.setValue(null);
	}

	@Override
	public final void setValue(Getter<Long> value) {
		this.nativePtr = null;
		super.setValue(value);
	}

	public final Int64rec setValue(long value) {
		setValue(holder(value));
		return this;
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateInt64(
				getEnclosing().getAllocation(),
				this,
				getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {

		final Getter<Ptr<AnyOp>> nativePtr = getNativePtr();

		if (nativePtr != null) {
			writer.writeNativePtrAsInt64(
					getAllocation(),
					nativePtr);
		} else {
			writer.writeInt64(getAllocation(), getValue());
		}
	}

}
