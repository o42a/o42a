/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.Rescopable;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.value.ValueStructIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.impl.*;


public abstract class ValueStruct<S extends ValueStruct<S, T>, T>
		implements ValueStructFinder, Rescopable<S> {

	public static final SingleValueStruct<Void> VOID =
			VoidValueStruct.INSTANCE;
	public static final SingleValueStruct<Long> INTEGER =
			IntegerValueStruct.INSTANCE;
	public static final SingleValueStruct<Double> FLOAT =
			FloatValueStruct.INSTANCE;
	public static final SingleValueStruct<String> STRING =
			StringValueStruct.INSTANCE;
	public static final SingleValueStruct<Directive> DIRECTIVE =
			DirectiveValueStruct.INSTANCE;

	public static final SingleValueStruct<java.lang.Void> NONE =
			NoneValueStruct.INSTANCE;

	private final ValueType<S> valueType;
	private final Class<? extends T> valueClass;

	private final RuntimeValue<T> runtimeValue = new RuntimeValue<T>(this);
	private final FalseValue<T> falseValue = new FalseValue<T>(this);
	private final UnknownValue<T> unknownValue = new UnknownValue<T>(this);

	private ValueStructIR<S, T> ir;

	public ValueStruct(ValueType<S> valueType, Class<? extends T> valueClass) {
		this.valueType = valueType;
		this.valueClass = valueClass;
	}

	public ValueType<S> getValueType() {
		return this.valueType;
	}

	public final Class<? extends T> getValueClass() {
		return this.valueClass;
	}

	public final boolean isVoid() {
		return ((ValueStruct<?, ?>) this) == VOID;
	}

	public final boolean isNone() {
		return ((ValueStruct<?, ?>) this) == NONE;
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

	public final ValueDef constantDef(
			Obj source,
			LocationInfo location,
			T value) {
		return new ConstantValueDef<T>(source, location, constantValue(value));
	}

	public final Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope) {
		return Definitions.noValueDefinitions(location, scope, this);
	}

	public abstract TypeRelation relationTo(ValueStruct<?, ?> other);

	public boolean assignableFrom(ValueStruct<?, ?> other) {
		return relationTo(other).isAscendant();
	}

	public abstract boolean convertibleFrom(ValueStruct<?, ?> other);

	public final T cast(Object value) {
		return getValueClass().cast(value);
	}

	@SuppressWarnings("unchecked")
	public final Value<T> cast(Value<?> value) {
		if (!assignableFrom(value.getValueStruct())) {
			throw new ClassCastException(
					value + " is not compatible with " + this);
		}
		return (Value<T>) value;
	}

	public abstract ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct);

	public abstract boolean isScoped();

	public abstract S reproduce(Reproducer reproducer);

	@Override
	public final S valueStructBy(Ref ref, ValueStruct<?, ?> defaultStruct) {
		return toValueStruct();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final S toValueStruct() {
		return (S) this;
	}

	public final boolean assertAssignableFrom(ValueStruct<?, ?> other) {
		assert assignableFrom(other) :
			this + " is not assignable from " + other;
		return true;
	}

	public final boolean assertIs(ValueStruct<?, ?> other) {
		assert this == other :
			this + " is not " + other;
		return true;
	}

	public String valueString(T value) {
		return value.toString();
	}

	public final ValueStructIR<S, T> ir(Generator generator) {

		final ValueStructIR<S, T> ir = this.ir;

		if (ir != null && ir.getGenerator() == generator) {
			return ir;
		}

		return this.ir = createIR(generator);
	}

	protected abstract void resolveAll(Value<T> value, Resolver resolver);

	protected abstract ValueStructIR<S, T> createIR(Generator generator);

}
