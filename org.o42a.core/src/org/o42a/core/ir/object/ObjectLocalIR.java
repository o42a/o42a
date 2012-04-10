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

import org.o42a.codegen.Generator;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.ir.object.impl.LocalIRFunc;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.Command;


public abstract class ObjectLocalIR extends ScopeIR {

	private final LocalIRFunc function;

	public ObjectLocalIR(Generator generator, LocalScope scope) {
		super(generator, scope);

		final LocalIR localIR = (LocalIR) this;
		final ObjectValueIR ownerValueIR =
				localIR.getOwnerIR().getObjectValueIR();

		this.function = new LocalIRFunc(localIR, ownerValueIR.getLocals());
	}

	public ValOp writeValue(
			ValDirs dirs,
			ObjOp owner,
			ObjOp ownerBody,
			Command command) {
		return this.function.call(dirs, owner, ownerBody, command);
	}

}
