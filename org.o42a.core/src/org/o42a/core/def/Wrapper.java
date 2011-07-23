/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.def;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


final class Wrapper extends Rescoper {

	private final Scope wrapped;

	Wrapper(Scope scope, Scope wrapped) {
		super(scope);
		this.wrapped = wrapped;
	}

	@Override
	public Scope rescope(Scope scope) {
		return this.wrapped;
	}

	@Override
	public Resolver rescope(LocationInfo location, Resolver resolver) {
		return this.wrapped.walkingResolver(resolver);
	}

	@Override
	public Scope updateScope(Scope scope) {
		return getFinalScope();
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {
		return host;
	}

	@Override
	public Rescoper reproduce(LocationInfo location, Reproducer reproducer) {
		location.getContext().getLogger().notReproducible(location);
		return null;
	}

	@Override
	public void resolveAll(ScopeInfo location, Resolver resolver) {
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + getFinalScope().hashCode();
		result = prime * result + this.wrapped.hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Wrapper other = (Wrapper) obj;

		if (getFinalScope() != other.getFinalScope()) {
			return false;
		}
		if (this.wrapped != other.wrapped) {
			return false;
		}

		return true;
	}

}
