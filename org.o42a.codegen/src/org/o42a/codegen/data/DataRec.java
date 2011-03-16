/*
    Compiler Code Generator
    Copyright (C) 2011 Ruslan Lopatin

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
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class DataRec<O extends DataOp> extends PtrRec<O> {

	DataRec(
			SubData<?> enclosing,
			CodeId id,
			Content<?> content) {
		super(enclosing, id, content);
	}

	@Override
	public final DataType getDataType() {
		return DataType.DATA_PTR;
	}

	public Type<?> getType() {
		return null;
	}

	static final class Rec extends DataRec<DataOp> {

		Rec(SubData<?> enclosing, CodeId id, Content<DataRec<DataOp>> content) {
			super(enclosing, id, content);
		}

		@Override
		public void setNull() {
			setValue(new Ptr<DataOp>(
					getGenerator().dataWriter().nullDataPtr()));
		}

		@Override
		protected void allocate(DataAllocator allocator) {
			setAllocation(allocator.allocateDataPtr(
					getEnclosing().getAllocation(),
					getAllocation()));
		}

		@Override
		protected void write(DataWriter writer) {
			fill(writer);
			getValue().getAllocation().write(writer);
		}

	}

}
