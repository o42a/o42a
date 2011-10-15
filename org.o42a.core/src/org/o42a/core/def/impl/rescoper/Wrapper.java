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
package org.o42a.core.def.impl.rescoper;

import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Reproducer;


public final class Wrapper extends Rescoper {

	private final Scope wrapped;

	public Wrapper(Scope scope, Scope wrapped) {
		super(scope);
		this.wrapped = wrapped;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public Scope rescope(Scope scope) {
		return this.wrapped;
	}

	@Override
	public Resolver rescope(Resolver resolver) {
		return this.wrapped.walkingResolver(resolver);
	}

	@Override
	public Scope updateScope(Scope scope) {
		return getFinalScope();
	}

	@Override
	public void resolveAll(Resolver resolver) {
	}

	@Override
	public Rescoper reproduce(Reproducer reproducer) {
		reproducer.getLogger().notReproducible(this.wrapped);
		return null;
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {
		return host;
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
