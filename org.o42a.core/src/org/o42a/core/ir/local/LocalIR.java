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
package org.o42a.core.ir.local;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectLocalIR;
import org.o42a.core.member.local.LocalScope;


public final class LocalIR extends ObjectLocalIR {

	public LocalIR(Generator generator, LocalScope scope) {
		super(generator, scope);
		scope.assertExplicit();
	}

	@Override
	public final LocalScope getScope() {
		return (LocalScope) super.getScope();
	}

	public final ObjectIR getOwnerIR() {
		return getScope().getOwner().ir(getGenerator());
	}

	@Override
	public void allocate() {
		getOwnerIR().allocate();
	}

	@Override
	public LocalOp op(CodeBuilder builder, Code code) {
		return (LocalOp) super.op(builder, code);
	}

	@Override
	protected void targetAllocated() {
		allocate();
	}

	@Override
	protected LocalOp createOp(CodeBuilder builder, Code code) {
		return new LocalOp(builder, getScope());
	}

}
