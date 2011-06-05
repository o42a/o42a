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
package org.o42a.core.value;

import static org.o42a.core.ir.op.ValOp.VAL_TYPE;
import static org.o42a.core.ref.path.Path.ROOT_PATH;

import java.util.HashMap;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.Intrinsics;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.op.Val;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ir.op.ValOp.Type;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


public abstract class ValueType<T> {

	public static final ValueType<Void> VOID = new VoidValueType();
	public static final ValueType<Long> INTEGER = new IntegerValueType();
	public static final ValueType<Double> FLOAT = new FloatValueType();
	public static final ValueType<String> STRING = new StringValueType();

	public static final ValueType<java.lang.Void> NONE = new None();

	private final String systemId;
	private final Class<? extends T> valueClass;
	private final RuntimeValue<T> runtimeValue = new RuntimeValue<T>(this);
	private final FalseValue<T> falseValue = new FalseValue<T>(this);
	private final UnknownValue<T> unknownValue = new UnknownValue<T>(this);

	private Generator cachedGenerator;
	private final HashMap<T, Ptr<ValOp>> constCache =
		new HashMap<T, Ptr<ValOp>>();

	ValueType(String systemId, Class<? extends T> valueClass) {
		this.systemId = systemId;
		this.valueClass = valueClass;
	}

	public final String getSystemId() {
		return this.systemId;
	}

	public final boolean isVoid() {
		return this == VOID;
	}

	public final Class<? extends T> getValueClass() {
		return this.valueClass;
	}

	@SuppressWarnings("unchecked")
	public final Value<T> cast(Value<?> value) {
		if (value.getValueType() != this) {
			throw new ClassCastException(
					value + " has incompatible type: " + value.getValueType()
					+ ", but " + this + " expected");
		}
		return (Value<T>) value;
	}

	public final T cast(Object value) {
		return getValueClass().cast(value);
	}

	public final T definiteValue(Value<?> value) {
		if (value.isVoid() && !isVoid()) {
			return null;
		}
		return cast(value).getDefiniteValue();
	}

	public abstract Obj wrapper(Intrinsics intrinsics);

	public StaticTypeRef typeRef(LocationInfo location, Scope scope) {

		final Distributor distributor = scope.distribute();
		@SuppressWarnings("unchecked")
		final Field<Obj> wrapperField =
			(Field<Obj>) wrapper(location.getContext().getIntrinsics())
			.getScope().toField();

		return ROOT_PATH.append(wrapperField.getKey())
		.target(location, distributor)
		.toStaticTypeRef();
	}

	public final Value<T> constantValue(T value) {
		return new ConstantValue<T>(this, value);
	}

	public final Value<T> runtimeValue() {
		return this.runtimeValue;
	}

	public final Value<T> falseValue() {
		return this.falseValue;
	}

	public final Value<T> unknownValue() {
		return this.unknownValue;
	}

	public final Ref constantRef(
			LocationInfo location,
			Distributor distributor,
			T value) {
		return new ConstantRef<T>(
				location,
				distributor,
				this,
				value);
	}

	public final Obj constantObject(
			LocationInfo location,
			Distributor enclosing,
			T value) {
		return new ConstantObject<T>(
				location,
				enclosing,
				this,
				value);
	}

	public final Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope) {
		return Definitions.noValueDefinitions(location, scope, this);
	}

	public String valueString(T value) {
		return value.toString();
	}

	@Override
	public String toString() {
		return getSystemId();
	}

	protected abstract Val val(Generator generator, T value);

	protected Ptr<ValOp> valPtr(Generator generator, T value) {
		if (this.cachedGenerator != generator) {
			this.constCache.clear();
			this.cachedGenerator = generator;
		} else {

			final Ptr<ValOp> cached = this.constCache.get(value);

			if (cached != null) {
				return cached;
			}
		}

		final Global<ValOp, Type> global =
			generator.newGlobal().setConstant().dontExport().newInstance(
					constId(generator, value),
					VAL_TYPE,
					val(generator, value));
		final Ptr<ValOp> result = global.getPointer();

		this.constCache.put(value, result);

		return result;
	}

	protected abstract CodeId constId(Generator generator, T value);

	private static final class None extends ValueType<java.lang.Void> {

		private None() {
			super("none", java.lang.Void.class);
		}

		@Override
		public Obj wrapper(Intrinsics intrinsics) {
			return null;
		}

		@Override
		protected Val val(Generator generator, java.lang.Void value) {
			throw new UnsupportedOperationException(
					"Type NONE can not have a value");
		}

		@Override
		protected Ptr<ValOp> valPtr(Generator generator, java.lang.Void value) {
			throw new UnsupportedOperationException(
					"Type NONE can not have a value");
		}

		@Override
		protected CodeId constId(Generator generator, java.lang.Void value) {
			throw new UnsupportedOperationException(
					"Type NONE can not have a value");
		}

	}

}
