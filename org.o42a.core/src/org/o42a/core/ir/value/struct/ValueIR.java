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
package org.o42a.core.ir.value.struct;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.value.impl.DefaultValueOp;
import org.o42a.core.value.ValueStruct;


public abstract class ValueIR<O extends ValueOp> {

	private final ObjectIR objectIR;
	private final ValueStruct<?, ?> valueStruct;

	public ValueIR(ValueStruct<?, ?> valueStruct, ObjectIR objectIR) {
		assert valueStruct != null :
			"Value structo not specified";
		assert objectIR != null :
			"Body not specified";
		this.valueStruct = valueStruct;
		this.objectIR = objectIR;
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return this.valueStruct;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public abstract Fld allocateBody(
			ObjectBodyIR bodyIR,
			SubData<?> data);

	public abstract void allocateMethods(
			ObjectMethodsIR methodsIR,
			SubData<?> data);

	public abstract O op(ObjectOp object);

	@Override
	public String toString() {
		if (this.objectIR == null) {
			return super.toString();
		}
		return "ValueIR[" + this.objectIR.getObject() + ']';
	}

	protected ValueOp defaultOp(ObjectOp object) {
		return new DefaultValueOp(this, object);
	}

}
