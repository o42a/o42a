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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.op.ObjectValFunc.OBJECT_VAL;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.local.*;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.ObjectValFunc;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;


public final class LocalIRFunc extends ObjectIRFunc {

	private final LocalIR localIR;
	private final CodeId id;
	private Function<ObjectValFunc> function;
	private final ObjectIRLocals locals;

	public LocalIRFunc(LocalIR localIR, ObjectIRLocals locals) {
		super(localIR.getOwnerIR());
		this.localIR = localIR;
		this.locals = locals;
		this.id = localIR.getId().detail("value");
	}

	public void call(
			Code code,
			ValOp result,
			ObjOp owner,
			ObjOp body) {
		if (body != null) {
			code.begin("Value for " + body);
		} else {
			code.begin("Value");
		}

		if (writeFalseValue(code, result, body)) {
			code.end();
			return;
		}

		code.debug("Call");

		final ObjectValFunc func = getFunction().getPointer().op(null, code);

		func.call(code, result, body(code, owner, body));

		code.end();
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

	public final CodeId getId() {
		return this.id;
	}

	public void build() {

		final LocalBuilder builder =
			new LocalBuilder(this.function, this.localIR);
		final ValOp result =
			this.function.arg(this.function, OBJECT_VAL.value());

		build(builder, this.function, result);

		this.function.done();
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	private void create() {

		final Function<ObjectValFunc> function =
			getGenerator().newFunction().create(
					getId(),
					OBJECT_VAL);

		function.debug("Calculating value");

		this.function = function;
	}

	private void build(LocalBuilder builder, Code code, ValOp result) {

		final StOp op = getScope().getBlock().op(builder);

		op.allocate(builder, code);

		final CodeBlk exit = code.addBlock("exit");
		final Control control = builder.createControl(code, exit.head());

		op.writeAssignment(control, result);

		if (exit.exists()) {
			result.storeFalse(exit);
			exit.returnVoid();
		}

		code.returnVoid();
	}

}
