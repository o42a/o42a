/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValHolderFactory.NO_VAL_HOLDER;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.field.object.FldCtrOp;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.StateOp;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


abstract class AbstractObjectValueBuilder
		implements FunctionBuilder<ObjectValueFunc> {

	private static final ID FLD_CTR_ID = ID.id("fld_ctr");

	@Override
	public void build(Function<ObjectValueFunc> function) {

		final ObjBuilder builder = builder(function);
		final ValOp result =
				function.arg(function, OBJECT_VALUE.value())
				.op(function, builder, getValueType(), VAL_TRAP);
		final ObjOp host = builder.host();
		final ValueOp value = host.value();

		assert getValueType().is(value.getValueType()) :
			"Wrong value type";

		final ObjectIRDataOp data = data(function, function);
		final StateOp state;
		final FldCtrOp ctr;
		final Block code;

		if (!getValueType().getDefaultStatefulness().isStateful()) {
			code = function;
			state = null;
			ctr = null;
		} else {
			code = function;
			state = value.state();
			ctr = writeKeptOrEval(code, state, result);
		}

		final Block done = code.addBlock("done");
		final Block exit = code.addBlock("exit");
		final DefDirs dirs =
				builder.dirs(code, exit.head())
				.value(result)
				.def(done.head());

		writeValue(dirs, host, data);

		final Block resultCode = dirs.done().code();

		if (resultCode.exists()) {
			resultCode.debug("Indefinite");
			result.storeFalse(resultCode);
			if (state != null && ctr != null) {
				state.initToFalse(resultCode);
				ctr.finish(resultCode, state.host());
			}
			resultCode.returnVoid();
		}
		if (exit.exists()) {
			exit.debug("False");
			result.storeFalse(exit);
			if (state != null && ctr != null) {
				state.initToFalse(exit);
				ctr.finish(exit, state.host());
			}
			exit.returnVoid();
		}
		if (done.exists()) {
			result.store(done, dirs.result());
			if (state != null && ctr != null) {
				state.init(done, result);
				ctr.finish(done, state.host());
			}
			done.returnVoid();
		}
	}

	protected abstract ValueType<?> getValueType();

	protected boolean lock() {
		return getValueType().getDefaultStatefulness().isStateful();
	}

	protected abstract ObjBuilder createBuilder(
			Function<ObjectValueFunc> function,
			CodePos failureDir);

	protected abstract ObjectIRDataOp data(
			Code code,
			Function<ObjectValueFunc> function);

	protected abstract void writeValue(
			DefDirs dirs,
			ObjOp host,
			ObjectIRDataOp data);

	private ObjBuilder builder(Function<ObjectValueFunc> function) {

		final Block failure = function.addBlock("failure");
		final ObjBuilder builder = createBuilder(function, failure.head());

		if (failure.exists()) {
			failure.debug("Failure");

			// No need to hold a false value.
			final ValOp result =
					function.arg(failure, OBJECT_VALUE.value())
					.op(function, builder, getValueType(), NO_VAL_HOLDER);

			result.storeFalse(failure);
			failure.returnVoid();
		}

		return builder;
	}

	private FldCtrOp writeKeptOrEval(
			Block code,
			StateOp state,
			ValOp result) {

		final Block valueKept = code.addBlock("value_kept");
		final FldCtrOp ctr = code.getAllocator().allocation().allocate(
				FLD_CTR_ID,
				FLD_CTR_TYPE);

		state.startEval(code, valueKept.head(), ctr);
		writeKept(valueKept, state, result);

		return ctr;
	}

	private void writeKept(Block code, StateOp state, ValOp result) {

		final Block falseKept = code.addBlock("false_kept");

		// Check the condition.
		state.loadCondition(code).goUnless(code, falseKept.head());

		// Return the value if condition is not false.
		final ValDirs dirs =
				state.getBuilder().dirs(code, falseKept.head())
				.value(result);

		state.loadValue(dirs, code);
		dirs.done().code().returnVoid();

		if (falseKept.exists()) {
			result.storeFalse(falseKept);
			falseKept.returnVoid();
		}
	}

}
