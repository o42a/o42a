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
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataWriter;


public final class StructPtrRec<O extends StructOp> extends PtrRec<O> {

	private final Type<O> type;
	private Generator generator;

	StructPtrRec(CodeId id, Type<O> type, Content<StructPtrRec<O>> content) {
		super(id, content);
		this.type = type;
	}

	public final Type<O> getType() {
		return this.type;
	}

	@Override
	public void setNull() {
		setValue(new Ptr<O>(this.generator.dataWriter().nullPtr(getType())));
	}

	@Override
	protected void allocate(Generator generator) {
		this.generator = generator;
		setAllocation(generator.dataAllocator().allocatePtr(
				getAllocation(),
				this.type.pointer(generator).getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		getValue().getAllocation().write(writer);
	}

}
