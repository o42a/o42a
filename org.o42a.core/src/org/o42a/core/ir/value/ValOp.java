/*
    Compiler Core
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
package org.o42a.core.ir.value;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.*;
import org.o42a.core.value.ValueType;


public final class ValOp extends IROp implements CondOp {

	private final ValueType<?> valueType;

	ValOp(CodeBuilder builder, ValType.Op ptr, ValueType<?> valueType) {
		super(builder, ptr);
		this.valueType = valueType;
	}

	public final ValueType<?> getValueType() {
		return this.valueType;
	}

	@Override
	public final ValType.Op ptr() {
		return (ValType.Op) super.ptr();
	}

	public final RecOp<Int32op> flags(CodeId id, Code code) {
		return ptr().flags(id, code);
	}

	@Override
	public final BoolOp loadCondition(CodeId id, Code code) {
		return ptr().loadCondition(id, code);
	}

	@Override
	public final BoolOp loadUnknown(CodeId id, Code code) {
		return ptr().loadUnknown(id, code);
	}

	public final BoolOp loadIndefinite(CodeId id, Code code) {
		return ptr().loadIndefinite(id, code);
	}

	public final RecOp<Int32op> length(CodeId id, Code code) {
		return ptr().length(id, code);
	}

	public final RecOp<Int64op> rawValue(CodeId id, Code code) {
		return ptr().rawValue(id, code);
	}

	public final AnyOp value(CodeId id, Code code) {
		return ptr().value(id, code);
	}

	public ValOp store(Code code, Val value) {
		ptr().store(code, value);
		return this;
	}

	public final ValOp storeVoid(Code code) {
		ptr().storeVoid(code);
		return this;
	}

	public final ValOp storeFalse(Code code) {
		ptr().storeFalse(code);
		return this;
	}

	public final ValOp storeUnknown(Code code) {
		ptr().storeUnknown(code);
		return this;
	}

	public final ValOp storeIndefinite(Code code) {
		ptr().storeIndefinite(code);
		return this;
	}

	public final ValOp store(Code code, ValOp value) {
		if (this != value) {
			ptr().store(code, value.ptr());
		}
		return this;
	}

	public final ValOp store(Code code, Int64op value) {
		ptr().store(code, value);
		return this;
	}

	public final ValOp store(Code code, Fp64op value) {
		ptr().store(code, value);
		return this;
	}

	public final void go(Code code, ValDirs dirs) {
		dirs.dirs().go(code, this);
	}

	@Override
	public final void go(Code code, CodeDirs dirs) {
		dirs.go(code, this);
	}

}
