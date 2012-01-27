/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ir.CodeBuilder.codeBuilder;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectRefFunc;
import org.o42a.core.ref.type.TypeRef;


public class ObjectTypeIRAncestorFunc
		implements FunctionBuilder<ObjectRefFunc> {

	private final ObjectIR objectIR;

	public ObjectTypeIRAncestorFunc(ObjectIR objectIR) {
		this.objectIR = objectIR;
	}

	@Override
	public void build(Function<ObjectRefFunc> function) {

		final Code failure = function.addBlock("failure");
		final TypeRef ancestor = this.objectIR.getObject().type().getAncestor();
		final CodeBuilder builder = codeBuilder(
				function,
				failure.head(),
				ancestor.getScope(),
				DERIVED);
		final CodeDirs dirs =
				builder.falseWhenUnknown(function, failure.head());
		final HostOp host = builder.host();

		ancestor.op(dirs, host)
		.target(dirs)
		.materialize(dirs)
		.toAny(function)
		.returnValue(function);

		if (failure.exists()) {
			failure.nullPtr().returnValue(failure);
		}
	}

}
