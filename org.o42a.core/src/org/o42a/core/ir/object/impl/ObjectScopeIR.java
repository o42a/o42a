/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import org.o42a.core.Container;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.object.Obj;


public final class ObjectScopeIR extends ScopeIR {

	public ObjectScopeIR(Generator generator, Obj object) {
		super(generator, object.getScope());
	}

	@Override
	public void allocate() {

		final Obj object = getScope().toObject();

		if (!object.getConstructionMode().isRuntime()
				|| object.getDereferencedLink() == null) {
			object.ir(getGenerator()).allocate();
		}
	}

	@Override
	protected void targetAllocated() {

		final Container enclosingContainer =
				getScope().getEnclosingContainer();

		if (enclosingContainer == null) {
			return;
		}

		enclosingContainer.getScope().ir(getGenerator()).allocate();
	}

	@Override
	protected HostOp createOp(CodeBuilder builder, Code code) {

		final Obj object = getScope().toObject();
		final ObjectIR objectIR = object.ir(getGenerator());

		return objectIR.op(builder, code);
	}

}
