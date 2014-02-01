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
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.*;


public final class ConstValOp extends ValOp {

	private final ValType.Op ptr;
	private final Val constant;

	public ConstValOp(CodeBuilder builder, ValType.Op ptr, Val constant) {
		super(builder, constant.getValueType());
		this.ptr = ptr;
		this.constant = constant;
	}

	@Override
	public final Val getConstant() {
		return this.constant;
	}

	@Override
	public final Allocator getAllocator() {
		throw new IllegalStateException("Constant value is not allocated");
	}

	@Override
	public final ValType.Op ptr() {
		return this.ptr;
	}

	@Override
	public final ValHolder holder() {
		throw new IllegalStateException("Constant value can not be held");
	}

	@Override
	public String toString() {
		if (this.constant == null) {
			return super.toString();
		}
		return this.constant.toString();
	}

}
