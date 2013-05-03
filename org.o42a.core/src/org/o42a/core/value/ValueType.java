/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import static org.o42a.core.value.impl.DefaultValueConverter.defaultValueConverter;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.directive.impl.DirectiveValueType;
import org.o42a.core.value.floats.FloatValueType;
import org.o42a.core.value.impl.NoneValueType;
import org.o42a.core.value.integer.IntegerValueType;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.link.impl.LinkByValueAdapter;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.impl.MacroValueType;
import org.o42a.core.value.string.StringValueType;
import org.o42a.core.value.voids.VoidValueType;


public abstract class ValueType<T> {

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

	private ValueTypeIR<T> ir;

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

	public final boolean is(ValueType<?> valueType) {
		return this == valueType;
	}

	public final boolean convertibleFrom(ValueType<?> other) {
		return getConverter().convertibleFrom(other);
	}

	public abstract Obj typeObject(Intrinsics intrinsics);

	public abstract Path path(Intrinsics intrinsics);

	public final StaticTypeRef typeRef(LocationInfo location, Scope scope) {
		return typeRef(location, scope, null);
	}

	public StaticTypeRef typeRef(
			LocationInfo location,
			Scope scope,
			TypeRefParameters typeParameters) {
		return path(location.getLocation().getContext().getIntrinsics())
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

	public String valueString(T value) {
		return value.toString();
	}

	public abstract ValueIRDesc irDesc();

	public final ValueTypeIR<T> ir(Generator generator) {

		final ValueTypeIR<T> ir = this.ir;

		if (ir != null && ir.getGenerator() == generator) {
			return ir;
		}

		return this.ir = createIR(generator);
	}

	@Override
	public String toString() {
		return getSystemId();
	}

	protected ValueConverter<T> getConverter() {
		return defaultValueConverter();
	}

	protected ValueAdapter defaultAdapter(
			Ref ref,
			TypeParameters<T> parameters,
			ValueRequest request) {

		final TypeParameters<?> expectedParameters =
				request.getExpectedParameters();
		final ValueType<?> expectedType = expectedParameters.getValueType();

		if (!request.isLinkByValueAllowed()
				|| expectedParameters.assignableFrom(parameters)) {
			return rawValueAdapter(ref);
		}

		final LinkValueType expectedLinkType = expectedType.toLinkType();

		if (expectedLinkType != null) {
			return new LinkByValueAdapter(
					adapterRef(
							ref,
							expectedLinkType.interfaceRef(expectedParameters),
							request.getLogger()),
					expectedLinkType.cast(expectedParameters));
		}

		final Ref adapter = adapterRef(
				ref,
				expectedParameters.getValueType().typeRef(ref, ref.getScope()),
				request.getLogger());

		return adapter.valueAdapter(request.noLinkByValue());
	}

	protected Ref adapterRef(
			Ref ref,
			TypeRef expectedTypeRef,
			CompilerLogger logger) {

		final Ref adapter = ref.adapt(ref, expectedTypeRef.toStatic(), logger);

		adapter.toTypeRef()
		.relationTo(expectedTypeRef)
		.checkDerived(logger);

		return adapter;
	}

	protected abstract ValueKnowledge valueKnowledge(T value);

	protected abstract Value<T> prefixValueWith(
			Value<T> value,
			PrefixPath prefix);

	protected abstract void resolveAll(Value<T> value, FullResolver resolver);

	protected abstract ValueTypeIR<T> createIR(Generator generator);

	protected abstract KeeperIR<?, ?> createKeeperIR(
			TypeParameters<T> parameters,
			ObjectIRBody bodyIR,
			Keeper keeper);

}
