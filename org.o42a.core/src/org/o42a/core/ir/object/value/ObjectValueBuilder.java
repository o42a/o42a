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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;

import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.value.ValueType;


final class ObjectValueBuilder extends AbstractObjectValueBuilder {

	private final ObjectValueFnIR fn;

	ObjectValueBuilder(ObjectValueFnIR fn) {
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
			Function<ObjectValueFn> function,
			CodePos failureDir) {
		return new ObjBuilder(
				function,
				failureDir,
				this.fn.getObjectIR(),
				DERIVED);
	}

	@Override
	protected void writeValue(
			DefDirs dirs,
			ObjOp host) {
		if (this.fn.getObjectIR().isExact()) {
			dirs.code().debug("Exact host: " + this.fn.getObjectIR().getId());
		} else {
			dirs.code().dumpName("Host: ", host);
		}

		this.fn.getValueIR().writeDef(dirs, host);
	}

}
