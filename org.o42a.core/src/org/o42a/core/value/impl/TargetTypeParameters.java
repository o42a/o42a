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

import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.util.log.Loggable;


public class TargetTypeParameters extends TypeParametersBuilder {

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
	public Scope getScope() {
		return this.target.getScope();
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
		return this.target.typeParameters(this.target.getScope())
				.refine(defaultParameters);
	}

	@Override
	public TargetTypeParameters prefixWith(PrefixPath prefix) {

		final Ref target = this.target.prefixWith(prefix);

		if (target == this.target) {
			return this;
		}

		return new TargetTypeParameters(target);
	}

	@Override
	public TargetTypeParameters reproduce(Reproducer reproducer) {

		final Ref target = this.target.reproduce(reproducer);

		if (target == null) {
			return null;
		}

		return new TargetTypeParameters(target);
	}

	@Override
	public String toString() {
		if (this.target == null) {
			return super.toString();
		}
		return "(`" + this.target + ')';
	}

}
