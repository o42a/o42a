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

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValHolderFactory.NO_VAL_HOLDER;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.BoolOp;
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
		final StateOp state = value.state();
		final FldCtrOp ctr;
		final BoolOp stateless;

		state.startEval(function, data);
		if (isStateful()) {
			ctr = writeKeptOrContinue(function, state, result);
			stateless = null;
		} else {

			final Block stateful = function.addBlock("stateful");

			stateless = state.flags().stateless(null, function);
			stateless.goUnless(function, stateful.head());
			if (!stateful.exists()) {
				ctr = null;
			} else {
				ctr = writeKeptOrContinue(stateful, state, result);
				stateful.go(function.tail());
			}
		}

		final Block done = function.addBlock("done");
		final Block exit = function.addBlock("exit");
		final DefDirs dirs =
				builder.dirs(function, exit.head())
				.value(result)
				.def(done.head());

		writeValue(dirs, host, data);

		final Block indefinite = dirs.done().code();

		if (indefinite.exists()) {
			indefinite.debug("Indefinite");
			result.storeFalse(indefinite);
			if (ctr != null) {
				returnIfStateless(indefinite, stateless);
				state.initToFalse(indefinite);
				ctr.finish(indefinite, state.host());
			}
			indefinite.returnVoid();
		}
		if (exit.exists()) {
			exit.debug("False");
			result.storeFalse(exit);
			if (ctr != null) {
				returnIfStateless(exit, stateless);
				state.initToFalse(exit);
				ctr.finish(exit, state.host());
			}
			exit.returnVoid();
		}
		if (done.exists()) {
			result.store(done, dirs.result());
			if (ctr != null) {
				returnIfStateless(done, stateless);
				state.init(done, result);
				ctr.finish(done, state.host());
			}
			done.returnVoid();
		}
	}

	protected abstract ValueType<?> getValueType();

	protected abstract boolean isStateful();

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

	private static FldCtrOp writeKeptOrContinue(
			Block code,
			StateOp state,
			ValOp result) {

		final Block valueKept = code.addBlock("value_kept");
		final FldCtrOp ctr = code.getAllocator().allocation().allocate(
				FLD_CTR_ID,
				FLD_CTR_TYPE);

		ctr.start(code, state.data()).goUnless(code, valueKept.head());
		writeKept(valueKept, state, result);

		return ctr;
	}

	private static void writeKept(Block code, StateOp state, ValOp result) {

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

	private static void returnIfStateless(Block code, BoolOp stateless) {
		if (stateless == null) {
			return;
		}

		final Block ret = code.addBlock("return");

		stateless.go(code, ret.head());
		if (ret.exists()) {
			ret.returnVoid();
		}
	}

}
