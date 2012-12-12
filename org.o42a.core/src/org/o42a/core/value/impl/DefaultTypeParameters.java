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
package org.o42a.core.value.impl;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.util.log.Loggable;


public final class DefaultTypeParameters extends TypeParametersBuilder {

	private final CompilerContext context;
	private final Loggable loggable;
	private final Scope scope;

	public DefaultTypeParameters(LocationInfo location, Scope scope) {
		this.context = location.getContext();
		this.loggable = location.getLoggable();
		this.scope = scope;
	}

	@Override
	public CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Loggable getLoggable() {
		return this.loggable;
	}

	@Override
	public Scope getScope() {
		return this.scope;
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
		return defaultParameters;
	}

	@Override
	public DefaultTypeParameters prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new DefaultTypeParameters(this, prefix.getStart());
	}

	@Override
	public DefaultTypeParameters reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new DefaultTypeParameters(this, reproducer.getScope());
	}

	@Override
	public String toString() {
		return "(`)";
	}

}
