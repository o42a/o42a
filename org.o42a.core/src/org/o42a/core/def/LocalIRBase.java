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
package org.o42a.core.def;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;


public abstract class LocalIRBase extends ScopeIR {

	public LocalIRBase(Generator generator, LocalScope scope) {
		super(generator, scope);
	}

	@Override
	public final LocalScope getScope() {
		return (LocalScope) super.getScope();
	}

	protected abstract void writeValue(
			Code code,
			CodePos exit,
			ValOp result,
			ObjOp owner,
			ObjOp ownerBody);

}
