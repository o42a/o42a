/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.object.impl.value;

import static org.o42a.core.ir.object.impl.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.object.op.ObjectDataCondFunc.OBJECT_DATA_COND;
import static org.o42a.core.ir.object.op.ObjectDataFunc.OBJECT_DATA;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.op.ObjectDataCondFunc;
import org.o42a.core.ir.object.op.ObjectDataFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueStruct;


public abstract class AbstractObjectValueBuilder
		implements FunctionBuilder<ObjectValueFunc> {

	@Override
	public void build(Function<ObjectValueFunc> function) {

		final ObjBuilder builder = builder(function);
		final boolean lock = lock();
		final ValOp result =
				function.arg(function, OBJECT_VALUE.value())
				.op(builder, getValueStruct())
				.setStoreMode(INITIAL_VAL_STORE);
		final ObjOp host = builder.host();
		final ObjectIRData.Op data = data(function);
		final ObjectDataFunc finishOp;

		if (!lock) {
			finishOp = null;
		} else {
			function.releaseBarrier();

			final FuncPtr<ObjectDataCondFunc> startFn =
					function.getGenerator().externalFunction().link(
							"o42a_obj_value_start",
							OBJECT_DATA_COND);
			final Block finish = function.addBlock("finish");

			startFn.op(null, function)
			.call(function, data)
			.goUnless(function, finish.head());

			final FuncPtr<ObjectDataFunc> finishFn =
					function.getGenerator().externalFunction().link(
							"o42a_obj_value_finish",
							OBJECT_DATA);

			finishOp = finishFn.op(null, function);
			finish.returnVoid();
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
			if (finishOp != null) {
				finishOp.call(code, data);
			}
			code.debug("Indefinite");
			code.returnVoid();
		}
		if (exit.exists()) {
			exit.debug("False");
			if (lock) {
				exit.releaseBarrier();
			}
			result.storeFalse(exit);
			if (finishOp != null) {
				finishOp.call(exit, data);
			}
			exit.returnVoid();
		}
		if (done.exists()) {
			result.store(done, dirs.result());
			if (finishOp != null) {
				finishOp.call(done, data);
			}
			done.returnVoid();
		}
	}

	protected abstract ValueStruct<?, ?> getValueStruct();

	protected abstract boolean lock();

	protected abstract ObjBuilder createBuilder(
			Function<ObjectValueFunc> function,
			CodePos failureDir);

	protected abstract ObjectIRData.Op data(Function<ObjectValueFunc> function);

	protected abstract void writeValue(
			DefDirs dirs,
			ObjOp host,
			ObjectIRData.Op data);

	private ObjBuilder builder(Function<ObjectValueFunc> function) {

		final Block failure = function.addBlock("failure");
		final ObjBuilder builder = createBuilder(function, failure.head());

		if (failure.exists()) {
			failure.debug("Failure");

			final ValOp result =
					function.arg(failure, OBJECT_VALUE.value())
					.op(builder, getValueStruct())
					.setStoreMode(INITIAL_VAL_STORE);

			if (lock()) {
				failure.releaseBarrier();
			}
			result.storeFalse(failure);
			failure.returnVoid();
		}

		return builder;
	}

}
