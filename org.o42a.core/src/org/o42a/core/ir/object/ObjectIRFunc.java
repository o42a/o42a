/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.LogicalDef;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.LogicalValue;


abstract class ObjectIRFunc {

	public static DataOp body(Code code, ObjOp host, ObjectOp body) {
		return body != null ? body.toData(code) : host.ptr().toData(code);
	}

	private final ObjectIR objectIR;

	ObjectIRFunc(ObjectIR objectIR) {
		this.objectIR = objectIR;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final Definitions definitions() {
		return getObjectIR().getObject().getDefinitions();
	}

	public boolean isExplicit(LogicalDef def) {
		if (def.isEmpty()) {
			return false;
		}
		return def.hasDefinitionsFrom(getObjectIR().getObject());
	}

	public final boolean isFalse(DefValue condition, ObjectOp body) {
		if (condition.isUnknown()) {
			return false;
		}
		return logicalValue(condition, body).isFalse();
	}

	public LogicalValue logicalValue(DefValue condition, ObjectOp body) {
		if (condition.isUnknown() || !condition.isDefinite()) {
			return LogicalValue.RUNTIME;
		}

		if (body == null || condition.isAlwaysMeaningful()) {
			if (condition.isRequirement() && condition.isFalse()) {
				// requirement is false
				return LogicalValue.FALSE;
			}
		}

		return LogicalValue.RUNTIME;
	}

	public boolean writeFalseValue(Code code, ValOp result, ObjectOp body) {

		final Obj object = getObjectIR().getObject();
		final Definitions definitions = object.getDefinitions();

		if (!object.isRuntime()) {
			if (isFalse(
					definitions.condition(definitions.getScope()),
					body)) {
				code.debug("Static object condition is false");
				result.storeFalse(code);
				return true;
			}
		} else {
			if (isFalse(
					definitions.requirement(definitions.getScope()),
					body)) {
				code.debug("Run-time object requirement is false");
				result.storeFalse(code);
				return true;
			}
		}

		return false;
	}

}
