/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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

import static org.o42a.core.ir.field.inst.InstFldKind.INST_LOCK;
import static org.o42a.core.ir.field.inst.ObjectIRLock.OBJECT_IR_LOCK;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.field.inst.ObjectIRLock;
import org.o42a.core.ir.object.ObjOp;


public class StaticObjectInitFn extends Fn<StaticObjectInitFn> {

	public static final
	ExtSignature<Void, StaticObjectInitFn> STATIC_OBJECT_INIT =
			customSignature("StaticObjectInitF", 2)
			.addData("object")
			.addPtr("lock", OBJECT_IR_LOCK)
			.returnVoid(c -> new StaticObjectInitFn(c));

	private StaticObjectInitFn(FuncCaller<StaticObjectInitFn> caller) {
		super(caller);
	}

	public final void init(Code code, ObjOp object) {

		final ObjectIRLock.Op lock;

		if (object.getObjectIR().bodies().findInstFld(INST_LOCK) == null) {
			lock = code.nullPtr(OBJECT_IR_LOCK);
		} else {
			lock = object.lock(code).ptr();
		}

		invoke(
				null,
				code,
				STATIC_OBJECT_INIT.result(),
				object.toData(null, code),
				lock);
	}

}
