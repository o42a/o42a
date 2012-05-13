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

import java.util.EnumMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Function;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRData.Op;
import org.o42a.core.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueStruct;


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
			extends AbstractObjectValueBuilder {

		private final CompilerContext context;
		private final PredefObjValue value;

		PredefValueBuilder(CompilerContext context, PredefObjValue value) {
			this.context = context;
			this.value = value;
		}

		@Override
		public void build(Function<ObjectValueFunc> function) {
			function.debug(this.value.toString());
			super.build(function);
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

		@Override
		protected ValueStruct<?, ?> getValueStruct() {
			return ValueStruct.VOID;
		}

		@Override
		protected boolean lock() {
			return true;
		}

		@Override
		protected ObjBuilder createBuilder(
				Function<ObjectValueFunc> function,
				CodePos failureDir) {

			final Obj voidObject = this.context.getVoid();

			return new ObjBuilder(
					function,
					failureDir,
					voidObject.ir(function.getGenerator()).getMainBodyIR(),
					voidObject,
					DERIVED);
		}

		@Override
		protected Op data(Function<ObjectValueFunc> function) {
			return function.arg(function, OBJECT_VALUE.data());
		}

		@Override
		protected void writeValue(DefDirs dirs, ObjOp host, Op data) {
			this.value.write(dirs, data);
		}

	}

}
