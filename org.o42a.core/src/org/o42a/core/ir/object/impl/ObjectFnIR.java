/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.impl;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;


public abstract class ObjectFnIR {

	private final ObjectIR objectIR;

	public ObjectFnIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final Obj getObject() {
		return getObjectIR().getObject();
	}

	public final Definitions definitions() {
		return getObject().value().getDefinitions();
	}

	public DataOp objectArg(Code code, ObjOp host) {

		final ObjectIR objectIR = getObjectIR();

		if (!objectIR.isExact()) {
			return host.toData(null, code);
		}

		assert host.getAscendant().is(objectIR.getObject()) :
			"Attempt to invoke " + this + " for object " + host.getAscendant()
			+ ", while " + getObject() + " expected";

		return code.nullDataPtr();
	}

}
