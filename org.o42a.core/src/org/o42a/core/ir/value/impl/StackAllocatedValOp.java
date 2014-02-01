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

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.*;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public final class StackAllocatedValOp extends ValOp {

	private final ID id;
	private final Allocator allocator;
	private final ValHolder holder;
	private ValType.Op ptr;

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
	}

	@Override
	public final ID getId() {
		return this.id;
	}

	@Override
	public final boolean isStackAllocated() {
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
	public ValType.Op ptr() {
		if (this.ptr != null) {
			return this.ptr;
		}

		final Code code = this.allocator.allocation();

		this.ptr = code.allocate(this.id.getLocal(), ValType.VAL_TYPE);
		storeIndefinite(code);

		return this.ptr;
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

}
