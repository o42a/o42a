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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.codegen.data.backend.DataWriter;


public final class Int64rec extends Rec<DataOp<Int64op>, Long> {

	private Ptr<AnyOp> nativePtr;

	Int64rec(String name, String id, Content<Int64rec> content) {
		super(name, id, content);
	}

	@Override
	public DataType getDataType() {
		return DataType.INT64;
	}

	public final Ptr<AnyOp> getNativePtr() {
		return this.nativePtr;
	}

	public final void setNativePtr(Ptr<AnyOp> nativePtr) {
		this.nativePtr = nativePtr;
		super.setValue(null);
	}

	@Override
	public void setValue(Long value) {
		this.nativePtr = null;
		super.setValue(value);
	}

	@Override
	protected void allocate(Generator generator) {
		setAllocation(generator.dataAllocator().allocateInt64(getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		if (this.nativePtr != null) {
			writer.writeNativePtrAsInt64(
					getAllocation(),
					this.nativePtr.getAllocation());
		} else {
			writer.writeInt64(getAllocation(), getValue());
		}
	}

}
