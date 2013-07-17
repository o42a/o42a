/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import static org.o42a.core.ir.object.ObjectTypeIR.OBJECT_DATA_ID;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRDataOp;
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
	protected boolean isStateful() {
		return this.fn.getObject().value().getStatefulness().isStateful();
	}

	@Override
	protected ObjBuilder createBuilder(
			Function<ObjectValueFunc> function,
			CodePos failureDir) {
		return new ObjBuilder(
				function,
				failureDir,
				this.fn.getObjectIR().getMainBodyIR(),
				this.fn.getObjectIR().getObject(),
				this.fn.getObjectIR().isExact() ? EXACT : DERIVED);
	}

	@Override
	protected ObjectIRDataOp data(
			Code code,
			Function<ObjectValueFunc> function) {
		if (!this.fn.getObjectIR().isExact()) {
			return function.arg(code, OBJECT_VALUE.data());
		}
		return this.fn.getObjectIR()
				.getTypeIR()
				.getObjectData()
				.pointer(this.fn.getGenerator())
				.op(OBJECT_DATA_ID, code);
	}

	@Override
	protected void writeValue(
			DefDirs dirs,
			ObjOp host,
			ObjectIRDataOp data) {
		if (this.fn.getObjectIR().isExact()) {
			dirs.code().debug("Exact host: " + this.fn.getObjectIR().getId());
		} else {
			dirs.code().dumpName("Host: ", host);
		}

		this.fn.getValueIR().writeClaim(dirs, host, null);
		this.fn.getValueIR().writeProposition(dirs, host, null);
	}

}
