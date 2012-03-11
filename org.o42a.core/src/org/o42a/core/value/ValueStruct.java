/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.value.ValueAdapter.rawValueAdapter;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.impl.LinkByValueAdapter;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.impl.*;


public abstract class ValueStruct<S extends ValueStruct<S, T>, T>
		implements ValueStructFinder {

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

	public final boolean isLink() {
		return getValueType().isLink();
	}

	public final Value<T> compilerValue(T value) {

		final ValueKnowledge knowledge = valueKnowledge(value);

		assert knowledge.hasCompilerValue() :
			"Incomplete knowledge (" + knowledge
			+ ") about value " + valueString(value);

		return new CompilerValue<T>(this, knowledge, value);
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

	public abstract ValueDef constantDef(
			Obj source,
			LocationInfo location,
			T value);

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

	public ValueAdapter defaultAdapter(
			Ref ref,
			ValueStruct<?, ?> expectedStruct) {
		if (expectedStruct == null || expectedStruct.assignableFrom(this)) {
			return rawValueAdapter(ref);
		}

		final LinkValueStruct expectedLinkStruct =
				expectedStruct.toLinkStruct();

		if (expectedLinkStruct != null) {
			return new LinkByValueAdapter(
					adapterRef(ref, expectedLinkStruct.getTypeRef()),
					expectedLinkStruct);
		}

		final Ref adapter = adapterRef(
				ref,
				expectedStruct.getValueType().typeRef(ref, ref.getScope()));

		return adapter.valueAdapter(null);
	}

	public Ref adapterRef(Ref ref, TypeRef expectedTypeRef) {

		final Ref adapter = ref.adapt(ref, expectedTypeRef.toStatic());

		adapter.toTypeRef().checkDerivedFrom(expectedTypeRef);

		return adapter;
	}

	public final boolean isScoped() {
		return toScoped() != null;
	}

	public abstract S prefixWith(PrefixPath prefix);

	public abstract S upgradeScope(Scope toScope);

	@Override
	public final S valueStructBy(Ref ref, ValueStruct<?, ?> defaultStruct) {
		return toValueStruct();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final S toValueStruct() {
		return (S) this;
	}

	public abstract ScopeInfo toScoped();

	public abstract LinkValueStruct toLinkStruct();

	public abstract S reproduce(Reproducer reproducer);

	public abstract void resolveAll(Resolver resolver);

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

	protected ValueStruct<S,T> applyParameters(TypeParameters parameters) {
		parameters.getLogger().error(
				"unsupported_type_parameters",
				parameters,
				"Type parameters not supported by %s",
				this);
		return this;
	}

	protected abstract ValueKnowledge valueKnowledge(T value);

	protected abstract Value<T> prefixValueWith(
			Value<T> value,
			PrefixPath prefix);

	protected abstract void resolveAll(Value<T> value, Resolver resolver);

	protected abstract ValueStructIR<S, T> createIR(Generator generator);

}
