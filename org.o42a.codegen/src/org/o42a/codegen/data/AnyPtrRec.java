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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataWriter;


public final class AnyPtrRec extends PtrRec<AnyOp> {

	private Generator generator;

	AnyPtrRec(CodeId id, Content<AnyPtrRec> content) {
		super(id, content);
	}

	@Override
	public void setNull() {
		setValue(new Ptr<AnyOp>(this.generator.dataWriter().nullPtr()));
	}

	@Override
	protected void allocate(Generator generator) {
		this.generator = generator;
		setAllocation(generator.dataAllocator().allocatePtr(
				getAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		fill(writer);
		getValue().getAllocation().write(writer);
	}

}
