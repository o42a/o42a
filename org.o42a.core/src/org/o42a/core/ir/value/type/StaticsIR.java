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
package org.o42a.core.ir.value.type;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueType;


public abstract class StaticsIR<T> {

	private final ValueTypeIR<T> valueTypeIR;

	public StaticsIR(ValueTypeIR<T> valueTypeIR) {
		this.valueTypeIR = valueTypeIR;
		assert valueTypeIR != null :
			"Value type not specified";
	}

	public final Generator getGenerator() {
		return getValueTypeIR().getGenerator();
	}

	public final ValueType<T> getValueType() {
		return getValueTypeIR().getValueType();
	}

	public final ValueTypeIR<T> getValueTypeIR() {
		return this.valueTypeIR;
	}

	public abstract Val val(T value);

	public abstract Ptr<ValType.Op> valPtr(T value);

	@Override
	public String toString() {
		if (this.valueTypeIR == null) {
			return super.toString();
		}
		return "StaticsIR[" + getValueType() + ']';
	}

}
