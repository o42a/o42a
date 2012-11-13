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
package org.o42a.core.ref.type;

import org.o42a.core.ScopeInfo;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.ArrayUtil;


public final class TypeParameters<T>
		extends Location
		implements TypeParametersBuilder {

	public static <T> TypeParameters<T> typeParameters(
			LocationInfo location,
			ValueType<?, T> valueType) {
		return new TypeParameters<T>(location, valueType);
	}

	private final ValueType<?, T> valueType;
	private final TypeParameter[] parameters;

	private TypeParameters(
			LocationInfo location,
			ValueType<?, T> valueType) {
		super(location);
		assert valueType != null :
			"Value type not specified";
		this.valueType = valueType;
		this.parameters = new TypeParameter[0];
	}

	private TypeParameters(
			LocationInfo location,
			ValueType<?, T> valueType,
			TypeParameter[] parameters) {
		super(location);
		this.valueType = valueType;
		assert parametersHaveSameScope(parameters);
		this.parameters = parameters;
	}

	public final ValueType<?, T> getValueType() {
		return this.valueType;
	}

	public final TypeParameter[] getParameters() {
		return this.parameters;
	}

	public final boolean isEmpty() {
		return this.parameters.length == 0;
	}

	public final boolean isValid() {
		for (TypeParameter parameter : getParameters()) {
			if (!parameter.isValid()) {
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
		if (this.parameters.length != 0) {
			parameter.assertSameScope(this.parameters[0]);
		}

		final TypeParameter param =
				new TypeParameter(key, this.parameters.length, parameter);

		return new TypeParameters<T>(
				this,
				getValueType(),
				ArrayUtil.append(this.parameters, param));
	}

	public final TypeParameter parameter(MemberKey key) {
		for (TypeParameter parameter : getParameters()) {
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

	public boolean assignableFrom(TypeParameters<?> parameters) {

		final TypeParameter[] params = getParameters();

		for (int i = 0; i < params.length; ++i) {

			final TypeRef typeRef = parameters.typeRef(params[i].getKey());

			if (typeRef == null) {
				return false;
			}
			if (!typeRef.derivedFrom(params[i].getTypeRef())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public final ValueStruct<?, ?> valueStructBy(TypeRef typeRef) {
		if (isEmpty()) {
			return typeRef.defaultValueStruct();
		}
		return typeRef.defaultValueStruct().setParameters(this);
	}

	@Override
	public TypeParameters<?> typeParametersBy(TypeRef typeRef) {
		if (isEmpty()) {
			return typeRef.defaultParameters();
		}
		return this;
	}

	@Override
	public final TypeParameters<T> prefixWith(PrefixPath prefix) {

		final TypeParameter[] oldParameters = getParameters();
		TypeParameter[] newParameters = null;

		for (int i = 0; i < oldParameters.length; ++i) {

			final TypeParameter oldParameter = oldParameters[i];
			final TypeParameter newParameter = oldParameter.prefixWith(prefix);

			if (oldParameter == newParameter) {
				continue;
			}
			if (newParameters == null) {
				newParameters = new TypeParameter[oldParameters.length];
				System.arraycopy(oldParameters, 0, newParameters, 0, i);
			}
			newParameters[i] = newParameter;
		}

		if (newParameters == null) {
			return this;
		}

		return new TypeParameters<T>(this, getValueType(), newParameters);
	}

	@Override
	public TypeParameters<T> reproduce(Reproducer reproducer) {

		final TypeParameter[] oldParameters = getParameters();
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

		return new TypeParameters<T>(this, getValueType(), newParameters);
	}

	public final boolean assertAssignableFrom(TypeParameters<?> parameters) {
		assert assignableFrom(parameters) :
			this + " is not assignable from " + parameters;
		return true;
	}

	public final boolean assertSameScope(ScopeInfo scope) {
		if (!isEmpty()) {
			this.parameters[0].assertSameScope(scope);
		}
		return true;
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.valueType).append("(`");
		if (this.parameters.length != 0) {
			out.append(this.parameters[0]);
			for (int i = 1; i < this.parameters.length; ++i) {
				out.append(", ");
				out.append(this.parameters[i]);
			}
		}
		out.append(')');

		return out.toString();
	}

	private static boolean parametersHaveSameScope(TypeParameter[] parameters) {
		if (parameters.length <= 1) {
			return true;
		}

		final TypeParameter first = parameters[0];

		for (int i = 1; i < parameters.length; ++i) {
			parameters[i].assertSameScope(first);
		}

		return true;
	}

}
