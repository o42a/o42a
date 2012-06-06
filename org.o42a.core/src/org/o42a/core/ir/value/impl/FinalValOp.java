/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.*;
import org.o42a.core.value.ValueStruct;


public final class FinalValOp extends ValOp {

	private final Allocator allocator;
	private final ValType.Op ptr;
	private final ValHolder holder;

	public FinalValOp(
			Allocator allocator,
			CodeBuilder builder,
			ValType.Op ptr,
			ValueStruct<?, ?> valueStruct,
			ValHolderFactory holderFactory) {
		super(builder, valueStruct);
		this.allocator = allocator;
		this.ptr = ptr;
		this.holder = holderFactory.createValHolder(this);
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
	public final ValType.Op ptr() {
		return this.ptr;
	}

	@Override
	public String toString() {

		final ValueStruct<?, ?> valueStruct = getValueStruct();

		if (valueStruct == null) {
			return super.toString();
		}

		return "(" + valueStruct + ") " + ptr();
	}

	@Override
	protected final ValHolder holder() {
		return this.holder;
	}

}
