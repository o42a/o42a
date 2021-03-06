/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.value.impl;

import static org.o42a.codegen.code.AllocationMode.ALLOCATOR_ALLOCATION;
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.*;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public final class StackAllocatedValOp extends ValOp {

	private final ID id;
	private final Allocator allocator;
	private final ValHolder holder;
	private final Allocated<ValType.Op> ptr;

	public StackAllocatedValOp(
			ID id,
			Allocator allocator,
			CodeBuilder builder,
			ValueType<?> valueType,
			ValHolderFactory holderFactory) {
		super(builder, valueType);
		this.id = id;
		this.allocator = allocator;
		this.holder = holderFactory.createValHolder(this);
		this.ptr = this.allocator.allocate(
				this.id.getLocal(),
				new AllocatableVal());
	}

	@Override
	public final ID getId() {
		return this.id;
	}

	@Override
	public final boolean isStackAllocated(Code code) {
		return true;
	}

	@Override
	public final Val getConstant() {
		return null;
	}

	@Override
	public final Allocator getAllocator() {
		return this.allocator;
	}

	@Override
	public ValType.Op ptr(Code code) {
		return this.ptr.get(code);
	}

	@Override
	public final ValHolder holder() {
		return this.holder;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return "(" + getValueType() + ") " + this.id;
	}

	private final class AllocatableVal implements Allocatable<ValType.Op> {

		@Override
		public AllocationMode getAllocationMode() {
			return ALLOCATOR_ALLOCATION;
		}

		@Override
		public Op allocate(Allocations code, Allocated<ValType.Op> allocated) {
			return code.allocate(ValType.VAL_TYPE);
		}

		@Override
		public void init(Code code, ValType.Op allocated) {
			allocated.flags(code, NOT_ATOMIC).storeIndefinite(code);
		}

		@Override
		public void dispose(Code code, Allocated<Op> allocated) {
		}

	}

}
