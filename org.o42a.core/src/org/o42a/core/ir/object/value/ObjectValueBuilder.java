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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.field.object.FldCtrOp.ALLOCATABLE_FLD_CTR;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Function;
import org.o42a.codegen.code.FunctionBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.field.object.FldCtrOp;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.StateOp;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.object.def.Def;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


final class ObjectValueBuilder implements FunctionBuilder<ObjectValueFn> {

	private static final ID FLD_CTR_ID = ID.id("fld_ctr");

	private final ObjectValueFnIR fnIR;

	ObjectValueBuilder(ObjectValueFnIR fnIR) {
		this.fnIR = fnIR;
	}

	public final ObjectValueFnIR fnIR() {
		return this.fnIR;
	}

	public final ValueType<?> getValueType() {
		return this.fnIR.getValueType();
	}

	public final boolean isStateful() {
		return getValueType().isStateful();
	}

	@Override
	public void build(Function<ObjectValueFn> function) {

		final ObjBuilder builder = fnIR().createBuilder(function);
		final ValOp result =
				function.arg(function, OBJECT_VALUE.value())
				.op(function, builder, getValueType(), VAL_TRAP);
		final ObjOp host = builder.host();
		final ValueOp value = host.value();

		assert getValueType().is(value.getValueType()) :
			"Wrong value type";

		final StateOp state = value.state();

		state.startEval(function);

		final FldCtrOp ctr =
				writeKeptOrContinue(function, state, result);
		final Block done = function.addBlock("done");
		final Block exit = function.addBlock("exit");
		final DefDirs dirs =
				builder.dirs(function, exit.head())
				.value(result)
				.def(done.head());

		writeValue(dirs, host);

		final Block indefinite = dirs.done().code();

		if (indefinite.exists()) {
			indefinite.debug("Indefinite");
			result.storeFalse(indefinite);
			if (isStateful()) {
				state.initToFalse(indefinite);
			}
			ctr.finish(indefinite, state.host());
			indefinite.returnVoid();
		}
		if (exit.exists()) {
			exit.debug("False");
			result.storeFalse(exit);
			if (isStateful()) {
				state.initToFalse(exit);
			}
			ctr.finish(exit, state.host());
			exit.returnVoid();
		}
		if (done.exists()) {
			result.store(done, dirs.result());
			done.dump("Result: ", result);
			if (isStateful()) {
				state.init(done, result);
			}
			ctr.finish(done, state.host());
			done.returnVoid();
		}
	}

	private static FldCtrOp writeKeptOrContinue(
			Block code,
			StateOp state,
			ValOp result) {

		final Block valueKept = code.addBlock("value_kept");
		final FldCtrOp ctr =
				code.allocate(FLD_CTR_ID, ALLOCATABLE_FLD_CTR).get(code);

		ctr.start(code, state.host()).goUnless(code, valueKept.head());
		writeKept(valueKept, state, result);

		return ctr;
	}

	private static void writeKept(Block code, StateOp state, ValOp result) {
		code.debug("Write kept");

		final Block falseKept = code.addBlock("false_kept");

		// Check the condition.
		state.loadCondition(code).goUnless(code, falseKept.head());

		// Return the value if condition is not false.
		state.loadValue(code, result);
		code.dump("Kept: ", state.value());
		code.returnVoid();

		if (falseKept.exists()) {
			falseKept.debug("False kept");
			result.storeFalse(falseKept);
			falseKept.returnVoid();
		}
	}

	private void writeValue(DefDirs dirs, ObjOp host) {
		if (fnIR().getObjectIR().isExact()) {
			dirs.code().debug("Exact host: " + fnIR().getObjectIR().getId());
		} else {
			dirs.code().dumpName("Host: ", host);
		}

		if (fnIR().writeIfConstant(dirs, fnIR().getFinal())) {
			return;
		}
		for (Def def : fnIR().defs().get()) {
			def.eval().write(dirs, host);
			if (!dirs.code().exists()) {
				break;
			}
		}
	}

}
