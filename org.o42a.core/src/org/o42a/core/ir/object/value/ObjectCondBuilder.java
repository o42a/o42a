/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;

import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.value.ValueType;


final class ObjectCondBuilder extends AbstractObjectCondBuilder {

	private final ObjectCondFnIR fn;

	ObjectCondBuilder(ObjectCondFnIR fn) {
		this.fn = fn;
	}

	@Override
	public String toString() {
		if (this.fn == null) {
			return super.toString();
		}
		return this.fn.toString();
	}

	@Override
	protected ValueType<?> getValueType() {
		return this.fn.getValueType();
	}

	@Override
	protected ObjBuilder createBuilder(
			Function<ObjectCondFn> function,
			CodePos failureDir) {
		return new ObjBuilder(
				function,
				failureDir,
				this.fn.getObjectIR(),
				this.fn.getObjectIR().isExact() ? EXACT : DERIVED);
	}

}
