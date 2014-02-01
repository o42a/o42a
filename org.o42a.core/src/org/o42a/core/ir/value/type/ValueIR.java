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
import org.o42a.core.ir.object.*;
import org.o42a.core.value.ValueType;


public abstract class ValueIR {

	private final ObjectIR objectIR;
	private final ValueTypeIR<?> valueTypeIR;

	public ValueIR(ValueTypeIR<?> valueTypeIR, ObjectIR objectIR) {
		assert valueTypeIR != null :
			"Value type not specified";
		assert objectIR != null :
			"Body not specified";
		this.valueTypeIR = valueTypeIR;
		this.objectIR = objectIR;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ValueType<?> getValueType() {
		return getValueTypeIR().getValueType();
	}

	public final ValueTypeIR<?> getValueTypeIR() {
		return this.valueTypeIR;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public abstract void allocateBody(ObjectIRBodyData data);

	public abstract ValueOp op(ObjectOp object);

	public abstract void setInitialValue(ObjectTypeIR type);

	@Override
	public String toString() {
		if (this.objectIR == null) {
			return super.toString();
		}
		return "ValueIR[" + this.objectIR.getObject() + ']';
	}

}
