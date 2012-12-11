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
package org.o42a.core.value.impl;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.util.log.Loggable;


public class TargetTypeParameters implements TypeParametersBuilder {

	private final Ref target;

	public TargetTypeParameters(Ref target) {
		this.target = target;
	}

	@Override
	public CompilerContext getContext() {
		return this.target.getContext();
	}

	@Override
	public Loggable getLoggable() {
		return this.target.getLoggable();
	}

	@Override
	public TypeParameters<?> refine(
			Obj object,
			TypeParameters<?> defaultParameters) {
		return this.target.typeParameters(object.getScope())
				.refine(object, defaultParameters);
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
		return this.target.typeParameters(this.target.getScope())
				.refine(defaultParameters);
	}

	@Override
	public TypeParametersBuilder prefixWith(PrefixPath prefix) {

		final Ref target = this.target.prefixWith(prefix);

		if (target == this.target) {
			return this;
		}

		return new TargetTypeParameters(target);
	}

	@Override
	public TypeParametersBuilder reproduce(Reproducer reproducer) {
		// TODO Auto-generated method stub
		return null;
	}

}
