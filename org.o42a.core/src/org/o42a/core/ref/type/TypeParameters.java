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

import java.util.Arrays;
import java.util.Iterator;

import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueStructFinder;


public final class TypeParameters
		extends Location
		implements ValueStructFinder, Iterable<TypeParameter> {

	private TypeParameter[] parameters;

	public TypeParameters(LocationInfo location, TypeParameter... parameters) {
		super(location);
		assert parametersHaveSameScope(parameters);
		this.parameters = parameters;
	}

	public final TypeParameter[] getParameters() {
		return this.parameters;
	}

	public final Object parameterKey(int index) {
		return Integer.valueOf(index);
	}

	public final TypeParameter parameter(Object key) {

		// TODO Implement the parameter key as field key.
		final int index = (Integer) key;

		if (this.parameters.length <= index) {
			return null;
		}

		return this.parameters[index];
	}

	public final TypeRef typeRef(Object key) {

		final TypeParameter parameter = parameter(key);

		if (parameter == null) {
			return null;
		}

		return parameter.getTypeRef();
	}

	@Override
	public final ValueStruct<?, ?> valueStructBy(
			ValueStruct<?, ?> defaultStruct) {
		return defaultStruct.setParameters(this);
	}

	@Override
	public final Iterator<TypeParameter> iterator() {
		return Arrays.asList(this.parameters).iterator();
	}

	@Override
	public final TypeParameters prefixWith(PrefixPath prefix) {

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

		return new TypeParameters(this, newParameters);
	}

	@Override
	public ValueStructFinder reproduce(Reproducer reproducer) {

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

		return new TypeParameters(this, newParameters);
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return "(`*)";
		}

		final StringBuilder out = new StringBuilder();

		out.append("(`");
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
