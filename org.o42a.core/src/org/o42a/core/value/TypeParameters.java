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
package org.o42a.core.value;

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.value.ValueAdapter.rawValueAdapter;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.*;
import org.o42a.core.ref.type.TypeRelation.Kind;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.impl.CompilerValue;
import org.o42a.core.value.impl.FalseValue;
import org.o42a.core.value.impl.RuntimeValue;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.macro.impl.MacroValueAdapter;
import org.o42a.core.value.voids.VoidValueAdapter;
import org.o42a.util.ArrayUtil;


public final class TypeParameters<T> extends TypeRefParameters {

	public static <T> TypeParameters<T> typeParameters(
			ScopeInfo location,
			ValueType<T> valueType) {
		return new TypeParameters<T>(location, location.getScope(), valueType);
	}

	public static <T> TypeParameters<T> typeParameters(
			LocationInfo location,
			Scope scope,
			ValueType<T> valueType) {
		return new TypeParameters<T>(location, scope, valueType);
	}

	private final Location location;
	private final Scope scope;
	private final ValueType<T> valueType;
	private final TypeParameter[] parameters;

	private TypeParameters(
			LocationInfo location,
			Scope scope,
			ValueType<T> valueType) {
		assert location != null :
			"Location not specified";
		assert scope != null :
			"Scope not specified";
		assert valueType != null :
			"Value type not specified";
		this.location = location.getLocation();
		this.scope = scope;
		this.valueType = valueType;
		this.parameters = new TypeParameter[0];
	}

	private TypeParameters(
			LocationInfo location,
			Scope scope,
			ValueType<T> valueType,
			TypeParameter[] parameters) {
		this.location = location.getLocation();
		this.scope = scope;
		this.valueType = valueType;
		assert parametersHaveSameScope(parameters);
		this.parameters = parameters;
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	@Override
	public final Scope getScope() {
		return this.scope;
	}

	public final ValueType<T> getValueType() {
		return this.valueType;
	}

	public TypeParameters<T> convertTo(ValueType<T> valueType) {
		if (valueType.is(getValueType())) {
			return this;
		}
		assert valueType.convertibleFrom(getValueType()) :
			valueType + " is not convertible from " + getValueType();
		return valueType.getConverter().convertParameters(this);
	}

	public final TypeParameter[] all() {
		return this.parameters;
	}

	public final int size() {
		return this.parameters.length;
	}

	public final boolean isEmpty() {
		return this.parameters.length == 0;
	}

	public final boolean isValid() {
		for (TypeParameter parameter : all()) {
			if (!parameter.isValid()) {
				return false;
			}
		}
		return true;
	}

	public final boolean validateAll() {
		for (TypeParameter parameter : all()) {
			if (!parameter.validateAll()) {
				return false;
			}
		}
		return true;
	}

	public final int getLinkDepth() {

		final LinkValueType linkType = getValueType().toLinkType();

		if (linkType == null) {
			return 0;
		}

		final MemberKey interfaceKey =
				linkType.interfaceKey(getContext().getIntrinsics());
		final TypeRef typeRef = typeRef(interfaceKey);

		if (typeRef == null) {
			return 1;
		}

		return 1 + typeRef.getParameters().getLinkDepth();
	}

	public final TypeParameters<T> add(MemberKey key, TypeRef parameter) {
		parameter.assertSameScope(this);

		final TypeParameter param = new TypeParameter(key, parameter);

		return new TypeParameters<T>(
				this,
				getScope(),
				getValueType(),
				ArrayUtil.append(this.parameters, param));
	}

	public final TypeParameter parameter(MemberKey key) {
		for (TypeParameter parameter : all()) {
			if (parameter.getKey().equals(key)) {
				return parameter;
			}
		}
		return null;
	}

	public final TypeRef typeRef(MemberKey key) {

		final TypeParameter parameter = parameter(key);

		if (parameter == null) {
			return null;
		}

		return parameter.getTypeRef();
	}

	public final Value<T> compilerValue(T value) {

		final ValueKnowledge knowledge = getValueType().valueKnowledge(value);

		assert knowledge.hasCompilerValue() :
			"Incomplete knowledge (" + knowledge
			+ ") about value " + getValueType().valueString(value);

		return new CompilerValue<T>(this, knowledge, value);
	}

	public final Value<T> runtimeValue() {
		return new RuntimeValue<T>(this);
	}

	public final Value<T> falseValue() {
		return new FalseValue<T>(this);
	}

	public final boolean assignableFrom(TypeParameters<?> other) {
		if (!getValueType().is(other.getValueType())) {
			return false;
		}
		return parametersAssignableFrom(other);
	}

	public final boolean convertibleFrom(TypeParameters<?> other) {
		if (!getValueType().convertibleFrom(other.getValueType())) {
			return false;
		}
		return getValueType().getConverter().convertibleParameters(this, other);
	}

	public TypeRelation.Kind relationTo(TypeParameters<?> other) {
		if (!getValueType().is(other.getValueType())) {
			return TypeRelation.Kind.INCOMPATIBLE;
		}

		final Kind relation1 = parametersRelation(other);

		if (!relation1.isError()) {
			return relation1;
		}

		final Kind relation2 = other.parametersRelation(this);

		if (!relation2.isError()) {
			return relation2.revert();
		}

		return relation1;
	}

	public final ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		if (request.getExpectedType().isVoid()) {
			if (getValueType().isVoid()) {
				return rawValueAdapter(ref);
			}
			return new VoidValueAdapter(ref);
		}
		if (request.getExpectedType().isMacro()) {
			if (getValueType().isMacro()) {
				return rawValueAdapter(ref);
			}
			return new MacroValueAdapter(ref);
		}
		return getValueType().defaultAdapter(ref, this, request);
	}

	@SuppressWarnings("unchecked")
	public final Value<T> cast(Value<?> value) {
		if (!assignableFrom(value.getTypeParameters())) {
			throw new ClassCastException(
					value + " is not compatible with " + this);
		}
		return (Value<T>) value;
	}

	public final Definitions noValueDefinitions(
			LocationInfo location,
			Scope scope) {
		return Definitions.noValueDefinitions(location, scope, this);
	}

	@Override
	public final TypeParameters<T> refine(TypeParameters<?> defaultParameters) {
		assertSameScope(defaultParameters);
		assert defaultParameters.getValueType().isVoid()
		|| getValueType().is(defaultParameters.getValueType()) :
			this + " value type is not compatible with "
			+ defaultParameters.getValueType();

		if (defaultParameters.isEmpty()) {
			// Always override the empty parameters.
			return this;
		}
		if (isEmpty()) {
			if (defaultParameters.getValueType().is(getValueType())) {
				return getValueType().cast(defaultParameters);
			}
			// Value type updated from VOID to something else.
			return new TypeParameters<T>(
					this,
					getScope(),
					getValueType(),
					defaultParameters.all());
		}

		TypeParameter[] newParameters = all();

		for (TypeParameter defaultParam : defaultParameters.all()) {
			if (parameter(defaultParam.getKey()) == null) {
				// Do only apply the default type parameter if it's not present
				// in this type parameters.
				newParameters = ArrayUtil.append(newParameters, defaultParam);
			}
		}

		if (newParameters.length == size()) {
			// This type parameters completely override the default ones.
			return this;
		}

		return new TypeParameters<T>(
				this,
				getScope(),
				getValueType(),
				newParameters);
	}

	@Override
	public final TypeParameters<T> prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}

		final TypeParameter[] oldParameters = all();
		final TypeParameter[] newParameters =
				new TypeParameter[oldParameters.length];

		for (int i = 0; i < oldParameters.length; ++i) {
			newParameters[i] = oldParameters[i].prefixWith(prefix);
		}

		return new TypeParameters<T>(
				this,
				prefix.getStart(),
				getValueType(),
				newParameters);
	}

	public final TypeParameters<T> upgradeScope(Scope toScope) {
		if (getScope().is(toScope)) {
			return this;
		}
		return prefixWith(upgradePrefix(this.scope, toScope));
	}

	@SuppressWarnings("unchecked")
	@Override
	public final TypeParameters<T> rescope(Scope toScope) {
		return (TypeParameters<T>) super.rescope(toScope);
	}

	public TypeParameters<T> rebuildIn(Scope scope) {
		assertCompatible(scope);

		final TypeParameter[] oldParameters = all();
		final TypeParameter[] newParameters =
				new TypeParameter[oldParameters.length];

		for (int i = 0; i < oldParameters.length; ++i) {
			newParameters[i] = oldParameters[i].rebuildIn(scope);
		}

		return new TypeParameters<T>(
				this,
				getScope(),
				getValueType(),
				newParameters);
	}

	public final TypeParameters<KnownLink> toLinkParameters() {

		final LinkValueType linkType = getValueType().toLinkType();

		if (linkType == null) {
			return null;
		}

		return linkType.cast(this);
	}

	public final TypeParameters<Array> toArrayParameters() {

		final ArrayValueType arrayType = getValueType().toArrayType();

		if (arrayType == null) {
			return null;
		}

		return arrayType.cast(this);
	}

	@Override
	public TypeParameters<T> reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final TypeParameter[] oldParameters = all();
		final TypeParameter[] newParameters =
				new TypeParameter[oldParameters.length];

		for (int i = 0; i < oldParameters.length; ++i) {

			final TypeParameter newParameter =
					oldParameters[i].reproduce(reproducer);

			if (newParameter == null) {
				return null;
			}
			newParameters[i] = newParameter;
		}

		return new TypeParameters<T>(
				this,
				reproducer.getScope(),
				getValueType(),
				newParameters);
	}

	public final void resolveAll(FullResolver resolver) {
		for (TypeParameter parameter : all()) {
			parameter.getTypeRef().resolveAll(resolver);
		}
	}

	public final KeeperIR<?, ?> keeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		return getValueType().createKeeperIR(this, bodyIR, keeper);
	}

	public final boolean assertAssignableFrom(TypeParameters<?> parameters) {
		assert assignableFrom(parameters) :
			this + " is not assignable from " + parameters;
		return true;
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.valueType);
		if (this.parameters.length != 0) {
			out.append("(`");
			out.append(this.parameters[0]);
			for (int i = 1; i < this.parameters.length; ++i) {
				out.append(", ");
				out.append(this.parameters[i]);
			}
			out.append(')');
		}

		return out.toString();
	}

	final boolean parametersAssignableFrom(TypeParameters<?> other) {
		for (TypeParameter parameter : all()) {

			final TypeRef typeRef = other.typeRef(parameter.getKey());

			if (typeRef == null) {
				continue;
			}
			if (!typeRef.derivedFrom(parameter.getTypeRef())) {
				return false;
			}
		}
		return true;
	}

	private boolean parametersHaveSameScope(TypeParameter[] parameters) {
		for (int i = 1; i < parameters.length; ++i) {
			parameters[i].assertSameScope(this);
		}
		return true;
	}

	private Kind parametersRelation(TypeParameters<?> other) {

		Kind result = TypeRelation.Kind.SAME;

		for (TypeParameter parameter : all()) {

			final TypeRef typeRef = other.typeRef(parameter.getKey());

			if (typeRef == null) {
				continue;
			}

			final Kind kind =
					parameter.getTypeRef().relationTo(typeRef).getKind();

			if (kind.isError()) {
				return kind;
			}
			if (result == null) {
				result = kind;
				continue;
			}
			if (kind.isAscendant() != result.isAscendant()) {
				return Kind.INCOMPATIBLE;
			}
			if (result.isSame()) {
				result = kind;
			}
		}

		return result;
	}

}
