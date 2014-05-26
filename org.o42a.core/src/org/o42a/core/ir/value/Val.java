/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static java.lang.Double.doubleToRawLongBits;

import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.value.ValueType;


public final class Val implements Content<ValType> {

	public static final int VAL_CONDITION = 0x01;
	public static final int VAL_INDEFINITE = 0x02;
	public static final int VAL_EAGER = 0x04;
	public static final int VAL_ALIGNMENT_MASK = 0x700;
	public static final int VAL_EXTERNAL = 0x800;
	public static final int VAL_STATIC = 0x1000;

	public static final Val VOID_VAL =
			new Val(ValueType.VOID, VAL_CONDITION, 0, 0L);
	public static final Val FALSE_VAL =
			new Val(ValueType.VOID, 0, 0, 0L);
	public static final Val INDEFINITE_VAL =
			new Val(ValueType.VOID, VAL_INDEFINITE, 0, 0L);

	public static Val falseVal(ValueType<?> valueType) {
		if (valueType.isVoid()) {
			return FALSE_VAL;
		}
		return new Val(valueType, 0, 0, 0L);
	}

	private final ValueType<?> valueType;
	private final int flags;
	private final int length;
	private final long value;
	private final Ptr<AnyOp> pointer;

	public Val(long value) {
		this.valueType = ValueType.INTEGER;
		this.flags = VAL_CONDITION;
		this.length = 0;
		this.value = value;
		this.pointer = null;
	}

	public Val(double value) {
		this.valueType = ValueType.FLOAT;
		this.flags = VAL_CONDITION;
		this.length = 0;
		this.value = doubleToRawLongBits(value);
		this.pointer = null;
	}

	public Val(
			ValueType<?> valueType,
			int flags,
			int length,
			long value) {
		this.valueType = valueType;
		this.flags = flags;
		this.length = length;
		this.value = value;
		this.pointer = null;
	}

	public Val(
			ValueType<?> valueType,
			int flags,
			int length,
			Ptr<AnyOp> pointer) {
		this.valueType = valueType;
		this.flags = flags;
		this.length = length;
		this.value = 0L;
		this.pointer = pointer;
	}

	public final ValueType<?> getValueType() {
		return this.valueType;
	}

	public final boolean isVoid() {
		return getValueType().isVoid();
	}

	public final int getFlags() {
		return this.flags;
	}

	public final Val setFlags(int flags) {
		if (this.pointer != null) {
			return new Val(this.valueType, flags, this.length, this.pointer);
		}
		return new Val(this.valueType, flags, this.length, this.value);
	}

	public final boolean getCondition() {
		return (this.flags & VAL_CONDITION) != 0;
	}

	public final boolean isIndefinite() {
		return (this.flags & VAL_INDEFINITE) != 0;
	}

	public final boolean isExternal() {
		return (this.flags & VAL_EXTERNAL) != 0;
	}

	public int getLength() {
		return this.length;
	}

	public final long getValue() {
		return this.value;
	}

	public final Ptr<AnyOp> getPointer() {
		return this.pointer;
	}

	@Override
	public void allocated(ValType instance) {
	}

	@Override
	public void fill(ValType instance) {
		instance.set(this);
	}

	@Override
	public String toString() {
		if (!getCondition()) {
			if (isIndefinite()) {
				return "indefinite";
			}
			return "false";
		}
		if (this.pointer != null) {
			if (this.length != 0) {
				return this.pointer + "[" + this.length + ']';
			}
			return this.pointer.toString();
		}
		if (this.length != 0) {
			return this.value + "[" + this.length + ']';
		}
		return String.valueOf(this.value);
	}

}
