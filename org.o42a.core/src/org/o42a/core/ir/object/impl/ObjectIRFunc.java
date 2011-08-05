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
package org.o42a.core.ir.object.impl;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.LogicalValue;


public abstract class ObjectIRFunc {

	public static ObjectOp body(Code code, ObjOp host, ObjectOp body) {
		return body != null ? body : host;
	}

	private final ObjectIR objectIR;

	public ObjectIRFunc(ObjectIR objectIR) {
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

	public boolean writeFalseValue(CodeDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final Definitions definitions = definitions();
		final Resolver resolver = definitions.getScope().dummyResolver();

		if (isFalse(definitions.requirements().resolve(resolver), body)) {
			code.debug("Object requirement is FALSE");
			code.go(dirs.falseDir());
			return true;
		}

		if (!getObject().getConstructionMode().isRuntime()) {
			if (isFalse(definitions.conditions().resolve(resolver), body)) {
				code.debug("Static object condition is FALSE");
				code.go(dirs.falseDir());
				return true;
			}
		}

		return false;
	}

}
