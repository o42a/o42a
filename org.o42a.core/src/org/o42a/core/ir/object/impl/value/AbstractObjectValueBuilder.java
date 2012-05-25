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

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.core.ir.object.impl.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.object.op.ObjectDataFunc.OBJECT_DATA;
import static org.o42a.core.ir.object.op.ObjectValueStartFunc.OBJECT_VALUE_START;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.op.ObjectDataFunc;
import org.o42a.core.ir.object.op.ObjectValueStartFunc;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.ValueOp;
import org.o42a.core.value.ValueStruct;


public abstract class AbstractObjectValueBuilder
		implements FunctionBuilder<ObjectValueFunc> {

	@Override
	public void build(Function<ObjectValueFunc> function) {

		final ObjBuilder builder = builder(function);
		final ValOp result =
				function.arg(function, OBJECT_VALUE.value())
				.op(builder, getValueStruct());
		final ObjOp host = builder.host();
		final ValueOp value = host.value();

		assert getValueStruct().getValueType() == value.getValueType() :
			"Wrong value type";

		final ObjectIRData.Op data = data(function, function);
		final ObjectDataFunc finishOp;

		if (!lock()) {
			finishOp = null;
		} else {

			final FuncPtr<ObjectValueStartFunc> startFn =
					function.getGenerator().externalFunction().link(
							"o42a_obj_value_start",
							OBJECT_VALUE_START);
			final FuncPtr<ObjectDataFunc> finishFn =
					function.getGenerator().externalFunction().link(
							"o42a_obj_value_finish",
							OBJECT_DATA);

			finishOp = finishFn.op(null, function);

			final Block finish = function.addBlock("finish");

			startFn.op(null, function)
			.call(function, result, data)
			.goUnless(function, finish.head());

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
			code.debug("Indefinite");
			result.storeFalse(code);
			if (finishOp != null) {
				value.initToFalse(code);
				finishOp.call(code, data);
			}
			code.returnVoid();
		}
		if (exit.exists()) {
			exit.debug("False");
			result.storeFalse(exit);
			if (finishOp != null) {
				value.initToFalse(exit);
				finishOp.call(exit, data);
			}
			exit.returnVoid();
		}
		if (done.exists()) {
			result.store(done, dirs.result());
			if (finishOp != null) {
				value.init(done, result);
				finishOp.call(done, data);
			}
			done.returnVoid();
		}
	}

	protected abstract ValueStruct<?, ?> getValueStruct();

	protected boolean lock() {
		return !getValueStruct().getValueType().isStateless();
	}

	protected abstract ObjBuilder createBuilder(
			Function<ObjectValueFunc> function,
			CodePos failureDir);

	protected abstract ObjectIRData.Op data(
			Code code,
			Function<ObjectValueFunc> function);

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
					.op(builder, getValueStruct());

			result.storeFalse(failure);
			if (lock()) {

				final ValType.Op value = data(failure, function).value(failure);
				final ValFlagsOp flags = value.flags(failure, ACQUIRE_RELEASE);

				flags.storeFalse(failure);
			}
			failure.returnVoid();
		}

		return builder;
	}

}
