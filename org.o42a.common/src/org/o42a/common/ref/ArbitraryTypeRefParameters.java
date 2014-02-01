/*
    Compiler Commons
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
package org.o42a.common.ref;

import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameter;
import org.o42a.core.value.TypeParameters;


public class ArbitraryTypeRefParameters extends TypeRefParameters {

	private final Location location;
	private final Scope scope;
	private final TypeRef[] parameters;

	public ArbitraryTypeRefParameters(
			LocationInfo location,
			Scope scope,
			TypeRef... parameters) {
		assert location != null :
			"Location not specified";
		assert scope != null :
			"Scope not specified";
		this.location = location.getLocation();
		this.scope = scope;
		this.parameters = parameters;
		assert assertParametersHaveSameScope();
	}

	@Override
	public final Scope getScope() {
		return this.scope;
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	public final TypeRef[] getParameters() {
		return this.parameters;
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {

		final TypeRef[] params = getParameters();

		if (params.length == 0) {
			return defaultParameters;
		}

		final int paramsRequired = defaultParameters.size();
		final int paramsToApply;

		if (params.length > paramsRequired) {
			redundantTypeParameter(params[paramsRequired]);
			paramsToApply = paramsRequired;
		} else {
			paramsToApply = params.length;
		}

		final TypeParameter[] oldParams = defaultParameters.all();
		TypeParameters<?> newParameters =
				typeParameters(this, defaultParameters.getValueType());

		for (int i = 0; i < paramsToApply; ++i) {
			newParameters = newParameters.add(oldParams[i].getKey(), params[i]);
		}
		for (int i = paramsToApply; i < paramsRequired; ++i) {

			final TypeParameter oldParam = oldParams[i];

			newParameters =
					newParameters.add(oldParam.getKey(), oldParam.getTypeRef());
		}

		return newParameters;
	}

	@Override
	public ArbitraryTypeRefParameters prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}

		final TypeRef[] oldParameters = getParameters();
		TypeRef[] newParameters = null;

		for (int i = 0; i < oldParameters.length; ++i) {

			final TypeRef oldParameter = oldParameters[i];
			final TypeRef newParameter = oldParameter.prefixWith(prefix);

			if (oldParameter == newParameter) {
				continue;
			}
			if (newParameters == null) {
				newParameters = new TypeRef[oldParameters.length];
				System.arraycopy(oldParameters, 0, newParameters, 0, i);
			}
			newParameters[i] = newParameter;
		}

		if (newParameters == null) {
			return this;
		}

		return new ArbitraryTypeRefParameters(
				this,
				prefix.getStart(),
				newParameters);
	}

	@Override
	public TypeRefParameters reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final TypeRef[] oldParameters = getParameters();
		final TypeRef[] newParameters = new TypeRef[oldParameters.length];

		for (int i = 0; i < oldParameters.length; ++i) {

			final TypeRef newParameter =
					oldParameters[i].reproduce(reproducer);

			if (newParameter == null) {
				return null;
			}
			newParameters[i] = newParameter;
		}

		return new ArbitraryTypeRefParameters(
				this,
				reproducer.getScope(),
				newParameters);
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return super.toString();
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

	private boolean assertParametersHaveSameScope() {
		for (TypeRef parameter : this.parameters) {
			parameter.assertSameScope(this);
		}
		return true;
	}

	private void redundantTypeParameter(TypeRef param) {
		param.getLogger().error(
				"redundant_type_parameter",
				param,
				"Redundant type parameter");
	}

}
