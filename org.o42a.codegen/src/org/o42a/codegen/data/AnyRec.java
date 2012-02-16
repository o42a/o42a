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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public final class AnyRec extends PtrRec<AnyRecOp, Ptr<AnyOp>> {

	AnyRec(SubData<?> enclosing, CodeId id, Content<AnyRec> content) {
		super(enclosing, id, content);
	}

	@Override
	public final DataType getDataType() {
		return DataType.PTR;
	}

	@Override
	public final AnyRec setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final AnyRec setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final AnyRec setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	@Override
	public final void setNull() {
		setValue(getGenerator().getGlobals().nullPtr());
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocatePtr(
				getEnclosing().getAllocation(),
				this,
				getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		getValue().get().getAllocation().write(writer, getAllocation());
	}

}
