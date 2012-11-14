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
package org.o42a.core.ir.object.impl;

import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.local.*;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.Command;
import org.o42a.util.string.ID;


public final class LocalFnIR
		extends ObjectFnIR
		implements FunctionBuilder<ObjectValFunc> {

	private final LocalIR localIR;
	private final ID id;
	private Function<ObjectValFunc> function;
	private final ObjectIRLocals locals;
	private Command command;

	public LocalFnIR(LocalIR localIR, ObjectIRLocals locals) {
		super(localIR.getOwnerIR());
		this.localIR = localIR;
		this.locals = locals;
		this.id = localIR.getScope().getId().detail("value");
	}

	public void call(DefDirs dirs, ObjOp owner, ObjOp body, Command command) {
		this.command = command;

		final DefDirs subDirs =
				dirs.begin(null, body != null ? "Value for " + body : "Value");
		final Code code = subDirs.code();
		final ObjectValFunc func = getFunction().getPointer().op(null, code);

		code.debug("Call");
		func.call(subDirs, objectArg(code, owner, body));
		subDirs.done();
	}

	public final LocalScope getScope() {
		return this.localIR.getScope();
	}

	public final Function<ObjectValFunc> getFunction() {
		if (this.function != null) {
			return this.function;
		}
		create();
		this.locals.addLocal(this);
		return this.function;
	}

	public final ID getId() {
		return this.id;
	}

	@Override
	public void build(Function<ObjectValFunc> function) {

		final LocalBuilder builder =
				new LocalBuilder(function, this.localIR);
		final ValType.Op value =
				this.function.arg(function, OBJECT_VAL.value());
		final ValOp result = value.op(
				function,
				builder,
				this.locals.getValueIR().getObject().value().getValueType(),
				VAL_TRAP);

		build(builder, function, result);
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	private void create() {

		final Function<ObjectValFunc> function =
				getGenerator().newFunction().create(
						getId(),
						OBJECT_VAL,
						this);

		function.debug("Calculating value");

		this.function = function;
	}

	private void build(LocalBuilder builder, Block code, ValOp result) {

		final Cmd cmd = this.command.cmd();
		final Block exit = code.addBlock("exit");
		final Block failure = code.addBlock("failure");
		final Control control = builder.createControl(
				code,
				exit.head(),
				failure.head(),
				result);

		cmd.write(control);

		control.end();

		if (exit.exists()) {
			exit.returnVoid();
		}
		if (failure.exists()) {
			result.storeFalse(failure);
			failure.returnVoid();
		}

		code.returnVoid();
	}

}
