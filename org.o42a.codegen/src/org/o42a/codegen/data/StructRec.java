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
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public final class StructRec<S extends StructOp<S>>
		extends PtrRec<StructRecOp<S>, Ptr<S>> {

	private final Type<S> type;

	StructRec(
			SubData<?> enclosing,
			CodeId id,
			Type<S> type,
			Content<StructRec<S>> content) {
		super(enclosing, id, content);
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
	public final void setNull() {
		setValue(getGenerator().getGlobals().nullPtr(getType()));
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocatePtr(
				getEnclosing().getAllocation(),
				this,
				getAllocation(),
				this.type.pointer(getGenerator()).getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		assert getValue() != null :
			this + " has no value";
		getValue().getAllocation().write(writer, getAllocation());
	}

}
