/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir;

import static org.o42a.core.ir.object.op.StaticObjectInitFn.STATIC_OBJECT_INIT;

import java.util.IdentityHashMap;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.cmd.LocalOp;
import org.o42a.core.ir.cmd.LocalsCode;
import org.o42a.core.ir.object.op.StaticObjectInitFn;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.string.ID;


final class FunctionLocals extends LocalsCode {

	private static final ID INIT_STATIC_ID = ID.rawId("init_static");

	private final CodeBuilder builder;
	private Code initStaticObject;
	private IdentityHashMap<Local, LocalOp> locals;
	private boolean staticObjectInitialized;

	FunctionLocals(CodeBuilder builder) {
		this.builder = builder;
	}

	@Override
	public final LocalOp get(Local local) {

		final LocalOp op = this.locals != null ? this.locals.get(local) : null;

		assert op != null :
			"Local `" + local + "` did not evaluated yet";

		return op;
	}

	@Override
	public final LocalOp set(CodeDirs dirs, Local local, RefOp ref) {
		if (local.isMember()) {
			initStaticObject();
		}
		if (this.locals == null) {
			this.locals = new IdentityHashMap<>();
		}

		final LocalOp op = allocate(dirs, local, ref);
		final LocalOp old = this.locals.put(local, op);

		assert old == null :
			"Local " + local + " already evaluated";

		return op;
	}

	final void start() {
		if (this.builder.host()
				.getWellKnownType()
				.getConstructionMode()
				.isRuntime()) {
			// Not necessary for objects constructed at run time.
			this.staticObjectInitialized = true;
		} else {
			this.initStaticObject =
					this.builder.getFunction().inset(INIT_STATIC_ID);
		}
	}

	private void initStaticObject() {
		if (this.staticObjectInitialized) {
			return;
		}
		this.staticObjectInitialized = true;

		final Code code = this.initStaticObject;
		final StaticObjectInitFn fn =
				this.builder.getGenerator()
				.externalFunction()
				.link("o42a_obj_static", STATIC_OBJECT_INIT)
				.op(null, code);

		fn.init(code, this.builder.host());

		this.builder.host();
	}

}
