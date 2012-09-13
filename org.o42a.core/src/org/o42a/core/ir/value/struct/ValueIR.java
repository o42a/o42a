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
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.value.impl.DefaultValueOp;
import org.o42a.core.value.ValueStruct;


public abstract class ValueIR {

	private final ObjectIR objectIR;
	private final ValueStructIR<?, ?> valueStructIR;

	public ValueIR(ValueStructIR<?, ?> valueStructIR, ObjectIR objectIR) {
		assert valueStructIR != null :
			"Value struct not specified";
		assert objectIR != null :
			"Body not specified";
		this.valueStructIR = valueStructIR;
		this.objectIR = objectIR;
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return getValueStructIR().getValueStruct();
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ValueStructIR<?, ?> getValueStructIR() {
		return this.valueStructIR;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public abstract void allocateBody(ObjectIRBodyData data);

	public abstract ValueOp op(ObjectOp object);

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
