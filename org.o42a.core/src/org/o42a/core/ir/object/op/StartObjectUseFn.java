/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.op;

import static org.o42a.core.ir.object.op.ObjectUseOp.OBJECT_USE_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.object.ObjectOp;


public final class StartObjectUseFn extends Fn<StartObjectUseFn> {

	public static final
	ExtSignature<Void, StartObjectUseFn> START_OBJECT_USE =
			customSignature("StartObjectUseF", 2)
			.addPtr("use", OBJECT_USE_TYPE)
			.addData("object")
			.returnVoid(c -> new StartObjectUseFn(c));

	private StartObjectUseFn(FuncCaller<StartObjectUseFn> caller) {
		super(caller);
	}

	public final void use(Code code, ObjectUseOp.Op use, ObjectOp object) {
		invoke(
				null,
				code,
				START_OBJECT_USE.result(),
				use,
				object.toData(null, code));
	}

}
