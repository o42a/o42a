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

		if (!lock()) {
			state = null;
			ctr = null;
		} else {

			final Block alreadyFalse = function.addBlock("already_false");
			final ValDirs dirs =
					builder.dirs(function, alreadyFalse.head()).value(result);

			ctr = function.allocation().allocate(FLD_CTR_ID, FLD_CTR_TYPE);
			state = value.state(dirs.dirs());

			final Block finish = function.addBlock("finish");

			state.startEval(function, finish.head(), ctr);
			writeKeptValue(dirs, finish, state);
			finish.returnVoid();

			if (alreadyFalse.exists()) {
				result.storeFalse(alreadyFalse);
				alreadyFalse.returnVoid();
			}
		}

		final Block done = function.addBlock("done");
		final Block exit = function.addBlock("exit");
		final DefDirs dirs =
				builder.dirs(function, exit.head())
				.value(result)
				.def(done.head());

		writeValue(dirs, host, data);

		final Block code = dirs.done().code();

		if (code.exists()) {
			code.debug("Indefinite");
			result.storeFalse(code);
			if (state != null && ctr != null) {
				state.initToFalse(code);
				ctr.finish(code, state.host());
			}
			code.returnVoid();
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

	private void writeKeptValue(ValDirs dirs, Block code, StateOp state) {
		// Check the condition.
		state.loadCondition(code).goUnless(code, dirs.falseDir());
		// Return the value if condition is not false.
		dirs.value().store(code, state.loadValue(dirs, code));
	}

}
