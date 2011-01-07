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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.st.Reproducer;


final class CompoundRescoper extends Rescoper {

	private final Rescoper first;
	private final Rescoper second;

	CompoundRescoper(Rescoper first, Rescoper second) {
		super(second.getFinalScope());
		this.first = first;
		this.second = second;
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
	public Scope updateScope(Scope scope) {
		return this.second.updateScope(this.first.updateScope(scope));
	}

	@Override
	public Def updateDef(Def def) {
		return this.second.updateDef(this.first.updateDef(def));
	}

	@Override
	public Rescoper and(Rescoper filter) {
		return this.first.and(this.second.and(filter));
	}

	@Override
	public HostOp rescope(Code code, CodePos exit, HostOp host) {
		return this.first.rescope(
				code,
				exit,
				this.second.rescope(code, exit, host));
	}

	@Override
	public Rescoper reproduce(LocationSpec location, Reproducer reproducer) {
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
