/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeParameters;
import org.o42a.core.ref.type.TypeParametersBuilder;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.directive.impl.DirectiveValueType;
import org.o42a.core.value.floats.FloatValueType;
import org.o42a.core.value.impl.NoneValueType;
import org.o42a.core.value.integer.IntegerValueType;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.impl.MacroValueType;
import org.o42a.core.value.string.StringValueType;
import org.o42a.core.value.voids.VoidValueType;


public abstract class ValueType<S extends ValueStruct<S, T>, T> {

	public static final SingleValueType<Void> VOID =
			VoidValueType.INSTANCE;
	public static final SingleValueType<Long> INTEGER =
			IntegerValueType.INSTANCE;
	public static final SingleValueType<Double> FLOAT =
			FloatValueType.INSTANCE;
	public static final SingleValueType<String> STRING =
			StringValueType.INSTANCE;
	public static final SingleValueType<Directive> DIRECTIVE =
			DirectiveValueType.INSTANCE;
	public static final SingleValueType<Macro> MACRO =
			MacroValueType.INSTANCE;

	public static final SingleValueType<java.lang.Void> NONE =
			NoneValueType.INSTANCE;

	private final String systemId;
	private final Class<? extends T> valueClass;

	public ValueType(String systemId, Class<? extends T> valueClass) {
		this.systemId = systemId;
		this.valueClass = valueClass;
	}

	public final String getSystemId() {
		return this.systemId;
	}

	public final Class<? extends T> getValueClass() {
		return this.valueClass;
	}

	public final boolean isVoid() {
		return is(VOID);
	}

	public final boolean isNone() {
		return is(NONE);
	}

	public final boolean isMacro() {
		return is(MACRO);
	}

	public final boolean isLink() {
		return toLinkType() != null;
	}

	public final boolean isArray() {
		return toArrayType() != null;
	}

	public abstract boolean isStateful();

	public abstract boolean isVariable();

	public final boolean is(ValueType<?, ?> valueType) {
		return this == valueType;
	}

	public abstract boolean convertibleFrom(ValueType<?, ?> other);

	public abstract Obj typeObject(Intrinsics intrinsics);

	public abstract Path path(Intrinsics intrinsics);

	public final StaticTypeRef typeRef(LocationInfo location, Scope scope) {
		return typeRef(location, scope, null);
	}

	public StaticTypeRef typeRef(
			LocationInfo location,
			Scope scope,
			TypeParametersBuilder typeParameters) {
		return path(location.getContext().getIntrinsics())
				.bind(location, scope)
				.staticTypeRef(scope.distribute(), typeParameters);
	}

	public abstract LinkValueType toLinkType();

	public abstract ArrayValueType toArrayType();

	public final T cast(Object value) {
		return getValueClass().cast(value);
	}

	@SuppressWarnings("unchecked")
	public final Value<T> cast(Value<?> value) {
		if (!is(value.getValueType())) {
			throw new ClassCastException(
					value + " is not compatible with " + this);
		}
		return (Value<T>) value;
	}

	@SuppressWarnings("unchecked")
	public final TypeParameters<T> cast(TypeParameters<?> parameters) {
		if (!is(parameters.getValueType())) {
			throw new ClassCastException(
					parameters + " is not compatible with " + this);
		}
		return (TypeParameters<T>) parameters;
	}

	@Override
	public String toString() {
		return getSystemId();
	}

	protected abstract ValueKnowledge valueKnowledge(T value);

	protected abstract Value<T> prefixValueWith(
			Value<T> value,
			PrefixPath prefix);

	protected abstract void resolveAll(Value<T> value, FullResolver resolver);

}
