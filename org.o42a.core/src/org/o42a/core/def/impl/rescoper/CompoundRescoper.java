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
import org.o42a.core.ScopeInfo;
import org.o42a.core.def.Def;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class CompoundRescoper extends Rescoper {

	private final Rescoper first;
	private final Rescoper second;

	public CompoundRescoper(Rescoper first, Rescoper second) {
		super(second.getFinalScope());
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean isStatic() {
		return this.first.isStatic() && this.second.isStatic();
	}

	@Override
	public Definitions update(Definitions definitions) {
		return this.second.update(this.first.update(definitions));
	}

	@Override
	public Scope rescope(Scope scope) {
		return this.first.rescope(this.second.rescope(scope));
	}

	@Override
	public Resolver rescope(LocationInfo location, Resolver resolver) {

		final Resolver rescoped = this.second.rescope(location, resolver);

		if (rescoped == null) {
			return null;
		}

		return this.first.rescope(location, rescoped);
	}

	@Override
	public Scope updateScope(Scope scope) {
		return this.second.updateScope(this.first.updateScope(scope));
	}

	@Override
	public <D extends Def<D>> D updateDef(D def) {
		return this.second.updateDef(this.first.updateDef(def));
	}

	@Override
	public Rescoper and(Rescoper filter) {
		return this.first.and(this.second.and(filter));
	}

	@Override
	public Rescoper reproduce(LocationInfo location, Reproducer reproducer) {
		getFinalScope().assertCompatible(reproducer.getReproducingScope());

		final Rescoper firstRescoper =
			this.first.reproduce(location, reproducer);

		if (firstRescoper == null) {
			return null;
		}

		final Scope rescoped =
			this.first.rescope(reproducer.getReproducingScope());

		if (rescoped == null) {
			return null;
		}

		final Reproducer secondReproducer = reproducer.reproducerOf(rescoped);

		if (secondReproducer == null) {
			return null;
		}

		final Rescoper secondRescoper =
			this.second.reproduce(location, secondReproducer);

		if (secondRescoper == null) {
			return null;
		}

		return new CompoundRescoper(firstRescoper, secondRescoper);
	}

	@Override
	public void resolveAll(ScopeInfo location, Resolver resolver) {
		this.second.resolveAll(location, resolver);
		this.first.resolveAll(location, this.second.rescope(location, resolver));
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {
		return this.first.rescope(
				dirs,
				this.second.rescope(dirs, host));
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.first.hashCode();
		result = prime * result + this.second.hashCode();

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

		final CompoundRescoper other = (CompoundRescoper) obj;

		if (!this.first.equals(other.first)) {
			return false;
		}
		if (!this.second.equals(other.second)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return this.first + " & " + this.second;
	}

}
