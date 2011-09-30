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
package org.o42a.core.value;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ref.Resolver;


public abstract class Value<T> {

	public static final Value<Void> voidValue() {
		return ValueType.VOID.constantValue(Void.VOID);
	}

	public static final Value<Void> falseValue() {
		return ValueType.VOID.falseValue();
	}

	public static final Value<Void> unknownValue() {
		return ValueType.VOID.unknownValue();
	}

	private final ValueStruct<?, T> valueStruct;

	public Value(ValueStruct<?, T> valueStruct) {
		this.valueStruct = valueStruct;
	}

	public final ValueType<?> getValueType() {
		return this.valueStruct.getValueType();
	}

	public final ValueStruct<?, T> getValueStruct() {
		return this.valueStruct;
	}

	public final boolean isFalse() {
		return getCondition().isFalse();
	}

	public final boolean isUnknown() {
		return getCondition().isUnknown();
	}

	public final boolean isDefinite() {
		return getCondition().isConstant();
	}

	public final boolean isVoid() {
		return getValueType() == ValueType.VOID;
	}

	public abstract Condition getCondition();

	public abstract T getDefiniteValue();

	public abstract Val val(Generator generator);

	public abstract Ptr<ValType.Op> valPtr(Generator generator);

	public void resolveAll(Resolver resolver) {
		getValueStruct().resolveAll(this, resolver);
	}

	public final ValOp op(CodeBuilder builder, Code code) {
		assert isDefinite() :
			"An attempt to create an indefinite value constant";

		final Generator generator = code.getGenerator();
		final Ptr<ValType.Op> ptr = valPtr(generator);

		return ptr.op(ptr.getId(), code).op(builder, val(generator));
	}

	public String valueString() {

		final Condition condition = getCondition();

		if (!condition.isTrue()) {
			return condition.toString();
		}

		return getValueStruct().valueString(getDefiniteValue());
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(').append(this.valueStruct).append(") ");
		out.append(valueString());

		return out.toString();
	}

}
