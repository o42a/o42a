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
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeParameters;
import org.o42a.core.ref.type.TypeParametersBuilder;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.directive.impl.DirectiveValueStruct;
import org.o42a.core.value.floats.FloatValueStruct;
import org.o42a.core.value.impl.*;
import org.o42a.core.value.integer.IntegerValueStruct;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.core.value.link.impl.LinkByValueAdapter;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.impl.MacroValueAdapter;
import org.o42a.core.value.macro.impl.MacroValueStruct;
import org.o42a.core.value.string.StringValueStruct;
import org.o42a.core.value.voids.VoidValueAdapter;
import org.o42a.core.value.voids.VoidValueStruct;


public abstract class ValueStruct<S extends ValueStruct<S, T>, T>
		implements TypeParametersBuilder {

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
	public static final SingleValueStruct<Macro> MACRO =
			MacroValueStruct.INSTANCE;

	public static final SingleValueStruct<java.lang.Void> NONE =
			NoneValueStruct.INSTANCE;

	private final ValueType<S, T> valueType;

	private final RuntimeValue<T> runtimeValue = new RuntimeValue<T>(this);
	private final FalseValue<T> falseValue = new FalseValue<T>(this);

	private ValueStructIR<S, T> ir;

	public ValueStruct(ValueType<S, T> valueType) {
		this.valueType = valueType;
	}

	public ValueType<S, T> getValueType() {
		return this.valueType;
	}

	public final boolean isVoid() {
		return VOID.is(this);
	}

	public final boolean isNone() {
		return NONE.is(this);
	}

	public final boolean isMacro() {
		return MACRO.is(this);
	}

	public final boolean isLink() {
		return getValueType().isLink();
	}

	public abstract boolean isValid();

	public final boolean isVariable() {
		return getValueType().isVariable();
	}

	public abstract TypeParameters<T> getParameters();

	public ValueStruct<S,T> setParameters(TypeParameters<?> parameters) {
		parameters.getLogger().error(
				"unsupported_type_parameters",
				parameters,
				"Type parameters not supported by %s",
				this);
		return this;
	}

	public final Value<T> compilerValue(T value) {

		final ValueKnowledge knowledge = getValueType().valueKnowledge(value);

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

	public final Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope) {
		return Definitions.noValueDefinitions(location, scope, this);
	}

	public final ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		if (request.getExpectedStruct().isVoid()) {
			if (isVoid()) {
				return rawValueAdapter(ref);
			}
			return new VoidValueAdapter(ref);
		}
		if (request.getExpectedStruct().isMacro()) {
			if (isMacro()) {
				return rawValueAdapter(ref);
			}
			return new MacroValueAdapter(ref);
		}
		return defaultAdapter(ref, request);
	}

	public Ref adapterRef(
			Ref ref,
			TypeRef expectedTypeRef,
			CompilerLogger logger) {

		final Ref adapter = ref.adapt(ref, expectedTypeRef.toStatic(), logger);

		adapter.toTypeRef()
		.relationTo(expectedTypeRef)
		.checkDerived(logger);

		return adapter;
	}

	public final boolean isScoped() {
		return toScoped() != null;
	}

	@Override
	public abstract S prefixWith(PrefixPath prefix);

	public abstract S upgradeScope(Scope toScope);

	public abstract S rebuildIn(Scope scope);

	@Override
	@SuppressWarnings("unchecked")
	public final S valueStructBy(TypeRef typeRef) {
		return (S) this;
	}

	@Override
	public TypeParameters<T> typeParametersBy(TypeRef typeRef) {
		return getParameters();
	}

	public abstract ScopeInfo toScoped();

	public abstract LinkValueStruct toLinkStruct();

	public abstract ArrayValueStruct toArrayStruct();

	@Override
	public abstract S reproduce(Reproducer reproducer);

	public abstract void resolveAll(FullResolver resolver);

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

	protected ValueAdapter defaultAdapter(Ref ref, ValueRequest request) {

		final ValueStruct<?, ?> expectedStruct = request.getExpectedStruct();

		if (!request.isTransformAllowed()
				|| expectedStruct.getParameters()
				.assignableFrom(getParameters())) {
			return rawValueAdapter(ref);
		}

		final LinkValueStruct expectedLinkStruct =
				expectedStruct.toLinkStruct();

		if (expectedLinkStruct != null) {
			return new LinkByValueAdapter(
					adapterRef(
							ref,
							expectedLinkStruct.getTypeRef(),
							request.getLogger()),
					expectedLinkStruct);
		}

		final Ref adapter = adapterRef(
				ref,
				expectedStruct.getValueType().typeRef(ref, ref.getScope()),
				request.getLogger());

		return adapter.valueAdapter(request.dontTransofm());
	}

	protected abstract ValueStructIR<S, T> createIR(Generator generator);

}
