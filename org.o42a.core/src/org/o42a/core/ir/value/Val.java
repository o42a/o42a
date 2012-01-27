/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public final class Val implements Content<ValType> {

	public static final int CONDITION_FLAG = 0x01;
	public static final int UNKNOWN_FLAG = 0x02;
	public static final int INDEFINITE_FLAG = 0x04;
	public static final int ALIGNMENT_MASK = 0x700;
	public static final int EXTERNAL_FLAG = 0x800;
	public static final int STATIC_FLAG = 0x1000;

	public static final Val VOID_VAL =
			new Val(ValueStruct.VOID, CONDITION_FLAG, 0, 0L);
	public static final Val FALSE_VAL =
			new Val(ValueStruct.VOID, 0, 0, 0L);
	public static final Val UNKNOWN_VAL =
			new Val(ValueStruct.VOID, UNKNOWN_FLAG, 0, 0L);
	public static final Val INDEFINITE_VAL =
			new Val(ValueStruct.VOID, UNKNOWN_FLAG | INDEFINITE_FLAG, 0, 0L);

	private final ValueStruct<?, ?> valueStruct;
	private final int flags;
	private final int length;
	private final long value;
	private final Ptr<AnyOp> pointer;

	public Val(long value) {
		this.valueStruct = ValueStruct.INTEGER;
		this.flags = CONDITION_FLAG;
		this.length = 0;
		this.value = value;
		this.pointer = null;
	}

	public Val(double value) {
		this.valueStruct = ValueStruct.FLOAT;
		this.flags = CONDITION_FLAG;
		this.length = 0;
		this.value = doubleToRawLongBits(value);
		this.pointer = null;
	}

	public Val(
			ValueStruct<?, ?> valueStruct,
			int flags,
			int length,
			long value) {
		this.valueStruct = valueStruct;
		this.flags = flags;
		this.length = length;
		this.value = value;
		this.pointer = null;
	}

	public Val(
			ValueStruct<?, ?> valueStruct,
			int flags,
			int length,
			Ptr<AnyOp> pointer) {
		this.valueStruct = valueStruct;
		this.flags = flags;
		this.length = length;
		this.value = 0L;
		this.pointer = pointer;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return this.valueStruct;
	}

	public final boolean isVoid() {
		return getValueStruct().isVoid();
	}

	public final int getFlags() {
		return this.flags;
	}

	public final boolean getCondition() {
		return (this.flags & CONDITION_FLAG) != 0;
	}

	public final boolean isUnknown() {
		return (this.flags & UNKNOWN_FLAG) != 0;
	}

	public final boolean isIndefinite() {
		return (this.flags & INDEFINITE_FLAG) != 0;
	}

	public final boolean isExternal() {
		return (this.flags & EXTERNAL_FLAG) != 0;
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
			if (isUnknown()) {
				return "unknown";
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
