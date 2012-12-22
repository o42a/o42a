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
package org.o42a.core.ref.type.impl;

import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.Loggable;


public final class CompatibleTypeRefParameters extends TypeRefParameters {

	private final TypeRefParameters parameters;

	public CompatibleTypeRefParameters(TypeRefParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public Scope getScope() {
		return this.parameters.getScope();
	}

	@Override
	public CompilerContext getContext() {
		return this.parameters.getContext();
	}

	@Override
	public Loggable getLoggable() {
		return this.parameters.getLoggable();
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {

		final TypeParameters<?> parameters =
				this.parameters.refine(typeParameters(this, ValueType.VOID));

		return parameters.refineCompatible(defaultParameters);
	}

	@Override
	public TypeRefParameters removeIncompatible() {
		return this;
	}

	@Override
	public CompatibleTypeRefParameters prefixWith(PrefixPath prefix) {

		final TypeRefParameters parameters = this.parameters.prefixWith(prefix);

		if (parameters == this.parameters) {
			return this;
		}

		return new CompatibleTypeRefParameters(parameters);
	}

	@Override
	public CompatibleTypeRefParameters reproduce(Reproducer reproducer) {

		final TypeRefParameters parameters =
				this.parameters.reproduce(reproducer);

		if (parameters == null) {
			return null;
		}

		return new CompatibleTypeRefParameters(parameters);
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return super.toString();
		}
		return '*' + this.parameters.toString();
	}

}
