/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.object.type.ValueTypeDescOp.VALUE_TYPE_DESC_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.type.ValueTypeDescOp;
import org.o42a.core.value.ValueType;


public abstract class ValueTypeIR<T> {

	private final Generator generator;
	private final ValueType<T> valueType;
	private Ptr<ValueTypeDescOp> valueTypeDesc;
	private StaticsIR<T> staticsIR;

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

	public final Ptr<ValueTypeDescOp> getValueTypeDesc() {
		if (this.valueTypeDesc != null) {
			return this.valueTypeDesc;
		}
		return this.valueTypeDesc =
				getGenerator().externalGlobal().setConstant().link(
						"o42a_val_type_" + getValueType().getSystemId(),
						VALUE_TYPE_DESC_TYPE);
	}

	public StaticsIR<T> staticsIR() {
		if (this.staticsIR != null) {
			return this.staticsIR;
		}
		return this.staticsIR = createStaticsIR();
	}

	public abstract ValueIR valueIR(ObjectIR objectIR);

	@Override
	public String toString() {
		if (this.valueType == null) {
			return super.toString();
		}
		return "ValueTypeIR[" + this.valueType + ']';
	}

	protected abstract StaticsIR<T> createStaticsIR();

}
