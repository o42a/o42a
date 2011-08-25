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

import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.STATIC_ALLOC_CLASS;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocation;


public class Ptr<P extends PtrOp<P>> extends AbstractPtr {

	private DataAllocation<P> allocation;

	Ptr(Data<?> data, boolean ptrToConstant, boolean isNull) {
		super(data.getId(), ptrToConstant, isNull);
	}

	Ptr(
			CodeId id,
			DataAllocation<P> allocation,
			boolean ptrToConstant,
			boolean isNull) {
		super(id, ptrToConstant, isNull);
		this.allocation = allocation;
	}

	public final DataAllocation<P> getAllocation() {
		return this.allocation;
	}

	public final RelPtr relativeTo(Ptr<?> ptr) {
		return new RelPtr(this, ptr);
	}

	public final Ptr<DataOp> toData() {
		return new Ptr<DataOp>(
				getId().detail("struct"),
				this.allocation.toData(),
				isPtrToConstant(),
				isNull());
	}

	public final P op(CodeId id, Code code) {

		final CodeBase c = code;

		c.assertIncomplete();

		return getAllocation().op(
				id != null ? code.opId(id) : getId(),
				isPtrToConstant() ? CONSTANT_ALLOC_CLASS : STATIC_ALLOC_CLASS,
				c.writer());
	}

	@Override
	public String toString() {
		return this.allocation.toString();
	}

	@Override
	protected DataAllocation<AnyOp> allocationToAny() {
		return this.allocation.toAny();
	}

	final void setAllocation(DataAllocation<P> allocation) {
		this.allocation = allocation;
	}

	@SuppressWarnings("unchecked")
	final void copyAllocation(Data<?> data) {
		this.allocation = (DataAllocation<P>) data.getAllocation();
	}

}
