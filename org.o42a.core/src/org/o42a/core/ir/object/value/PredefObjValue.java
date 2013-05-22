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

import static org.o42a.core.ir.object.value.ObjectCondFunc.OBJECT_COND;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.object.value.PredefObjValues.predefObjValues;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Function;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public abstract class PredefObjValue {

	public static final PredefObjValue FALSE_OBJ_VALUE = new FalseObjValue();

	public static final PredefObjValue VOID_OBJ_VALUE = new VoidObjValue();

	public static final PredefObjValue STUB_OBJ_VALUE = new StubObjValue();

	public static final PredefObjValue DEFAULT_OBJ_VALUE =
			new DefaultObjValue();

	private static final ID OBJ_VALUE_ID = ID.id("_o42a_obj_value");
	private static final ID OBJ_COND_ID = ID.id("_o42a_obj_cond");

	private final boolean typeAware;

	PredefObjValue(boolean typeAware) {
		this.typeAware = typeAware;
	}

	public final boolean isTypeAware() {
		return this.typeAware;
	}

	public final FuncPtr<ObjectValueFunc> valueFunction(
			CompilerContext context,
			Generator generator,
			ValueType<?> valueType) {
		return predefObjValues(generator)
				.valueFunction(context, this, valueType);
	}

	public final FuncPtr<ObjectCondFunc> condFunction(
			CompilerContext context,
			Generator generator,
			ValueType<?> valueType) {
		return predefObjValues(generator)
				.condFunction(context, this, valueType);
	}

	abstract FuncPtr<ObjectValueFunc> createValueFunction(
			CompilerContext context,
			Generator generator,
			ValueType<?> valueType);

	abstract FuncPtr<ObjectCondFunc> createCondFunction(
			CompilerContext context,
			Generator generator,
			ValueType<?> valueType);

	private static final class FalseObjValue extends PredefObjValue {

		FalseObjValue() {
			super(false);
		}

		@Override
		public String toString() {
			return "FALSE_OBJ_VALUE";
		}

		@Override
		FuncPtr<ObjectValueFunc> createValueFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {
			return generator.externalFunction().link(
					"o42a_obj_value_false",
					OBJECT_VALUE);
		}

		@Override
		FuncPtr<ObjectCondFunc> createCondFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {
			return generator.externalFunction().link(
					"o42a_obj_cond_false",
					OBJECT_COND);
		}

	}

	private static final class VoidObjValue extends PredefObjValue {

		VoidObjValue() {
			super(false);
		}

		@Override
		public String toString() {
			return "VOID_OBJ_VALUE";
		}

		@Override
		FuncPtr<ObjectValueFunc> createValueFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {
			return generator.externalFunction().link(
					"o42a_obj_value_void",
					OBJECT_VALUE);
		}

		@Override
		FuncPtr<ObjectCondFunc> createCondFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {
			return generator.externalFunction().link(
					"o42a_obj_cond_true",
					OBJECT_COND);
		}

	}

	private static final class StubObjValue extends PredefObjValue {

		StubObjValue() {
			super(false);
		}

		@Override
		public String toString() {
			return "STUB_OBJ_VALUE";
		}

		@Override
		FuncPtr<ObjectValueFunc> createValueFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {
			return generator.externalFunction().link(
					"o42a_obj_value_stub",
					OBJECT_VALUE);
		}

		@Override
		FuncPtr<ObjectCondFunc> createCondFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {
			return generator.externalFunction().link(
					"o42a_obj_cond_stub",
					OBJECT_COND);
		}

	}

	private static final class DefaultObjValue extends PredefObjValue {

		DefaultObjValue() {
			super(true);
		}

		@Override
		public String toString() {
			return "DEFAULT_OBJ_VALUE";
		}

		@Override
		FuncPtr<ObjectValueFunc> createValueFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {

			final ID id = OBJ_VALUE_ID.sub(valueType.getSystemId());
			final PredefValueBuilder builder =
					new PredefValueBuilder(context, id, valueType);
			final Function<ObjectValueFunc> function =
					generator.newFunction().create(
							id,
							OBJECT_VALUE,
							builder);

			return function.getPointer();
		}

		@Override
		FuncPtr<ObjectCondFunc> createCondFunction(
				CompilerContext context,
				Generator generator,
				ValueType<?> valueType) {

			final ID id = OBJ_COND_ID.sub(valueType.getSystemId());
			final PredefCondBuilder builder =
					new PredefCondBuilder(context, id, valueType);
			final Function<ObjectCondFunc> function =
					generator.newFunction().create(id, OBJECT_COND, builder);

			return function.getPointer();
		}

	}

}
