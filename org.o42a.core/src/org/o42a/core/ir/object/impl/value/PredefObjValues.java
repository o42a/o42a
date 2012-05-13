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

import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.impl.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;
import static org.o42a.core.value.ValueStruct.VOID;

import java.util.EnumMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.source.CompilerContext;


final class PredefObjValues {

	static PredefObjValues predefObjValues(Generator generator) {

		final PredefObjValues existing =
				generator.getFeature(PredefObjValues.class);

		if (existing != null) {
			return existing;
		}

		final PredefObjValues values = new PredefObjValues(generator);

		generator.setFeature(PredefObjValues.class, values);

		return values;
	}

	private final Generator generator;
	private final EnumMap<PredefObjValue, FuncPtr<ObjectValueFunc>> cache =
			new EnumMap<PredefObjValue, FuncPtr<ObjectValueFunc>>(
						PredefObjValue.class);

	private PredefObjValues(Generator generator) {
		this.generator = generator;
	}

	FuncPtr<ObjectValueFunc> get(
			CompilerContext context,
			PredefObjValue value) {

		final FuncPtr<ObjectValueFunc> cached = this.cache.get(value);

		if (cached != null) {
			return cached;
		}

		final FuncPtr<ObjectValueFunc> function =
				this.generator.newFunction().create(
						value.codeId(this.generator),
						OBJECT_VALUE,
						new PredefValueBuilder(context, value)).getPointer();

		this.cache.put(value, function);

		return function;
	}

	private static final class PredefValueBuilder
			implements FunctionBuilder<ObjectValueFunc> {

		private final CompilerContext context;
		private final PredefObjValue value;

		PredefValueBuilder(CompilerContext context, PredefObjValue value) {
			this.context = context;
			this.value = value;
		}

		@Override
		public void build(Function<ObjectValueFunc> function) {
			function.debug(this.value.toString());

			final Block failure = function.addBlock("failure");
			final Block done = function.addBlock("done");
			final Obj voidObject = this.context.getVoid();
			final ObjBuilder builder = new ObjBuilder(
					function,
					failure.head(),
					voidObject.ir(function.getGenerator()).getMainBodyIR(),
					voidObject,
					DERIVED);
			final ValOp result =
					function.arg(function, OBJECT_VALUE.value())
					.op(builder, VOID)
					.setStoreMode(INITIAL_VAL_STORE);
			final ObjOp host = builder.host();
			final ObjectIRData.Op data =
					function.arg(function, OBJECT_VALUE.data());

			final DefDirs dirs =
					builder.dirs(function, failure.head())
					.value(result)
					.def(done.head());

			dirs.code().dumpName("Host: ", host);

			this.value.write(dirs, data);

			final Block code = dirs.done().code();

			if (code.exists()) {
				code.debug("Indefinite");
				code.returnVoid();
			}
			if (failure.exists()) {
				failure.debug("False");
				result.storeFalse(failure);
				failure.returnVoid();
			}
			if (done.exists()) {
				result.store(done, dirs.result());
				done.returnVoid();
			}
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

	}

}
