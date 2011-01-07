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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.def.LocalIRBase;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;


public abstract class ObjectLocalIR extends LocalIRBase {

	private final LocalIRFunc function;

	public ObjectLocalIR(IRGenerator generator, LocalScope scope) {
		super(generator, scope);
		this.function = new LocalIRFunc((LocalIR) this);
	}

	@Override
	protected void writeValue(
			Code code,
			CodePos exit,
			ValOp result,
			ObjOp owner,
			ObjOp ownerBody) {
		this.function.call(code, exit, result, owner, ownerBody);
	}

}
