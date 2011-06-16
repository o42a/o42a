/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.value.ValueType;


public abstract class ValueTypeIR<T> {

	private final Generator generator;
	private final ValueType<T> valueType;

	public ValueTypeIR(Generator generator, ValueType<T> valueType) {
		this.generator = generator;
		this.valueType = valueType;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final ValueType<T> getValueType() {
		return this.valueType;
	}

	public boolean hasValue() {
		return true;
	}

	public boolean hasLength() {
		return false;
	}

	public abstract Val val(T value);

	public abstract Ptr<ValType.Op> valPtr(T value);

	@Override
	public String toString() {
		if (this.valueType == null) {
			return super.toString();
		}
		return this.valueType + " IR";
	}

	protected void store(Code code, ValOp target, Val value) {
		target.flags(null, code).store(code, code.int32(value.getFlags()));
		if (!value.getCondition()) {
			return;
		}
		if (hasLength()) {
			target.length(null, code)
			.store(code, code.int32(value.getLength()));
		}
		if (hasValue()) {

			final Ptr<AnyOp> pointer = value.getPointer();

			if (pointer != null) {
				target.value(null, code)
				.toPtr(null, code)
				.store(code, pointer.op(null, code));
			} else {
				target.rawValue(null, code).store(
						code,
						code.int64(value.getValue()));
			}
		}
	}

	protected void store(Code code, ValOp target, ValOp value) {
		target.flags(null, code).store(
				code,
				value.flags(null, code).load(null, code));
		if (hasLength()) {
			target.length(null, code).store(
					code,
					value.length(null, code).load(null, code));
		}
		if (hasValue()) {
			target.rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code));
		}
	}

	protected void initialize(Code code, ValOp target, Val value) {
		store(code, target, value);
	}

	protected void initialize(Code code, ValOp target, ValOp value) {
		store(code, target, value);
	}

	protected void assign(Code code, ValOp target, Val value) {
		store(code, target, value);
	}

	protected void assign(Code code, ValOp target, ValOp value) {
		store(code, target, value);
	}

}
