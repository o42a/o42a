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

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.directive.impl.DirectiveValueStruct;
import org.o42a.core.value.floats.FloatValueStruct;
import org.o42a.core.value.impl.NoneValueStruct;
import org.o42a.core.value.integer.IntegerValueStruct;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.impl.MacroValueStruct;
import org.o42a.core.value.string.StringValueStruct;
import org.o42a.core.value.voids.VoidValueStruct;


@Deprecated
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

	private final ValueType<T> valueType;

	public ValueStruct(ValueType<T> valueType) {
		this.valueType = valueType;
	}

	public ValueType<T> getValueType() {
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
		return getParameters().compilerValue(value);
	}

	public final Value<T> runtimeValue() {
		return getParameters().runtimeValue();
	}

	public final Value<T> falseValue() {
		return getParameters().falseValue();
	}

	public final boolean isScoped() {
		return toScoped() != null;
	}

	@Override
	public abstract S prefixWith(PrefixPath prefix);

	public abstract S upgradeScope(Scope toScope);

	public abstract S rebuildIn(Scope scope);

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

}
