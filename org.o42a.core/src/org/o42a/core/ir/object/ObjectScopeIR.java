/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;


public final class ObjectScopeIR extends ScopeIR {

	private final CodeId id;

	public ObjectScopeIR(Generator generator, Obj object) {
		super(generator, object.getScope());

		final Scope enclosingScope = getScope().getEnclosingScope();

		assert !enclosingScope.isTopScope() :
			"Can not build IR for " + object;

		this.id = enclosingScope.ir(generator).nextAnonymousId();
	}

	@Override
	public CodeId getId() {
		return this.id;
	}

	@Override
	public void allocate() {
		getScope().toObject().ir(getGenerator()).allocate();
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
