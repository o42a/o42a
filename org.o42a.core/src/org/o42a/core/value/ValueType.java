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

import static org.o42a.core.ir.op.Val.VOID_VAL;
import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.Intrinsics;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Def;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.op.Val;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


public abstract class ValueType<T> {

	public static final ValueType<Void> VOID = new VoidType();
	public static final ValueType<Long> INTEGER = new IntegerType();
	public static final ValueType<Double> FLOAT = new FloatType();
	public static final ValueType<String> STRING = new StringType();

	public static final ValueType<java.lang.Void> NONE = new NoneType();

	private final String systemId;
	private final Class<? extends T> valueClass;
	private final RuntimeValue<T> runtimeValue = new RuntimeValue<T>(this);
	private final FalseValue<T> falseValue = new FalseValue<T>(this);
	private final UnknownValue<T> unknownValue = new UnknownValue<T>(this);

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

	public final Value<T> definiteValue(T value) {
		return new DefiniteValue<T>(this, value);
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

	public final Ref definiteRef(
			LocationInfo location,
			Distributor distributor,
			T value) {
		return new DefiniteRef<T>(
				location,
				distributor,
				this,
				value);
	}

	public final Ref runtimeRef(
			LocationInfo location,
			Distributor distributor) {
		return Ref.runtimeRef(location, distributor, this);
	}

	public final Def runtimeDef(
			LocationInfo location,
			Distributor distributor) {
		return runtimeRef(location, distributor).toDef();
	}

	public final Obj definiteObject(
			LocationInfo location,
			Distributor enclosing,
			T value) {
		return new DefiniteValue.DefiniteObject<T>(
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

	protected abstract Val val(IRGenerator generator, T value);

	@Override
	public String toString() {
		return getSystemId();
	}

	private static final class VoidType extends ValueType<Void> {

		VoidType() {
			super("void", Void.class);
		}

		@Override
		public Obj wrapper(Intrinsics intrinsics) {
			return intrinsics.getVoid();
		}

		@Override
		public StaticTypeRef typeRef(LocationInfo location, Scope scope) {
			return voidRef(location, scope.distribute()).toStaticTypeRef();
		}

		@Override
		protected Val val(IRGenerator generator, Void value) {
			return VOID_VAL;
		}

	}

	private static final class IntegerType extends ValueType<Long> {

		private IntegerType() {
			super("integer", Long.class);
		}

		@Override
		public Obj wrapper(Intrinsics intrinsics) {
			return intrinsics.getInteger();
		}

		@Override
		protected Val val(IRGenerator generator, Long value) {
			return new Val(value);
		}

	}

	private static final class FloatType extends ValueType<Double> {

		private FloatType() {
			super("float", Double.class);
		}

		@Override
		public Obj wrapper(Intrinsics intrinsics) {
			return intrinsics.getFloat();
		}

		@Override
		protected Val val(IRGenerator generator, Double value) {
			return new Val(value);
		}

	}

	private static final class NoneType extends ValueType<java.lang.Void> {

		private NoneType() {
			super("none", java.lang.Void.class);
		}

		@Override
		public Obj wrapper(Intrinsics intrinsics) {
			return null;
		}

		@Override
		protected Val val(IRGenerator generator, java.lang.Void value) {
			throw new IllegalStateException("Type NONE can not have a value");
		}

	}

}
