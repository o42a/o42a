/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref.type.impl;

import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameter;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


final class ValueTypeInterfaceParameters extends TypeRefParameters {

	private final TypeRefParameters parameters;

	ValueTypeInterfaceParameters(TypeRefParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public final Location getLocation() {
		return this.parameters.getLocation();
	}

	@Override
	public final Scope getScope() {
		return this.parameters.getScope();
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {

		final TypeParameters<?> parameters =
				this.parameters.refine(typeParameters(this, ValueType.VOID));

		return refineCompatible(parameters, defaultParameters);
	}

	@Override
	public ValueTypeInterfaceParameters prefixWith(PrefixPath prefix) {

		final TypeRefParameters parameters = this.parameters.prefixWith(prefix);

		if (parameters == this.parameters) {
			return this;
		}

		return new ValueTypeInterfaceParameters(parameters);
	}

	@Override
	public ValueTypeInterfaceParameters reproduce(Reproducer reproducer) {

		final TypeRefParameters parameters =
				this.parameters.reproduce(reproducer);

		if (parameters == null) {
			return null;
		}

		return new ValueTypeInterfaceParameters(parameters);
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return super.toString();
		}
		return '*' + this.parameters.toString();
	}

	public final <T> TypeParameters<T> refineCompatible(
			TypeParameters<?> refinement,
			TypeParameters<T> defaultParameters) {
		if (refinement.isEmpty()) {
			return defaultParameters;
		}

		TypeParameters<T> newParameters =
				typeParameters(refinement, defaultParameters.getValueType());

		for (TypeParameter defaultParam : defaultParameters.all()) {

			final MemberKey key = defaultParam.getKey();
			final TypeParameter refinedParam = refinement.parameter(key);
			final TypeParameter newParam =
					refinedParam != null ? refinedParam : defaultParam;

			newParameters = newParameters.add(key, newParam.getTypeRef());
		}

		return newParameters;
	}

}
