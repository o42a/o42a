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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocation;


public class Ptr<O extends PtrOp> extends AbstractPtr {

	private DataAllocation<O> allocation;

	Ptr(Data<?> data) {
	}

	Ptr(DataAllocation<O> allocation) {
		this.allocation = allocation;
	}

	public final DataAllocation<O> getAllocation() {
		return this.allocation;
	}

	public final RelPtr relativeTo(Ptr<?> ptr) {
		return new RelPtr(this, ptr);
	}

	public final O op(Code code) {

		final CodeBase c = code;

		c.assertIncomplete();

		return getAllocation().op(c.writer());
	}

	@Override
	public String toString() {
		return this.allocation.toString();
	}

	@Override
	protected DataAllocation<AnyOp> allocationToAny() {
		return this.allocation.toAny();
	}

	final void setAllocation(DataAllocation<O> allocation) {
		this.allocation = allocation;
	}

	@SuppressWarnings("unchecked")
	final void copyAllocation(Data<?> data) {
		this.allocation = (DataAllocation<O>) data.getAllocation();
	}

}
