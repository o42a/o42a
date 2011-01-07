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


final class UpgradeRescoper extends Rescoper {

	private final Scope fromScope;

	UpgradeRescoper(Scope fromScope, Scope toScope) {
		super(toScope);
		this.fromScope = fromScope;
	}

	@Override
	public Scope rescope(Scope scope) {
		scope.assertDerivedFrom(getFinalScope());
		return scope;
	}

	@Override
	public Scope updateScope(Scope scope) {
		return getFinalScope();
	}

	@Override
	public Rescoper and(Rescoper other) {
		if (other.getClass() != UpgradeRescoper.class) {
			return super.and(other);
		}

		final UpgradeRescoper filter2 = (UpgradeRescoper) other;

		if (filter2.fromScope != getFinalScope()) {
			return super.and(other);
		}

		return new UpgradeRescoper(this.fromScope, other.getFinalScope());
	}

	@Override
	public Rescoper reproduce(LocationSpec location, Reproducer reproducer) {
		getFinalScope().assertCompatible(reproducer.getReproducingScope());
		return new UpgradeRescoper(this.fromScope, reproducer.getScope());
	}

	@Override
	public HostOp rescope(Code code, CodePos exit, HostOp host) {
		code.debug("Upgrade scope " + host + " to " + this.fromScope);
		return host.toObject(code, exit).cast(
				code,
				exit, this.fromScope.getContainer().toObject());
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + getFinalScope().hashCode();
		result = prime * result + this.fromScope.hashCode();

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

		final UpgradeRescoper other = (UpgradeRescoper) obj;

		if (getFinalScope() != other.getFinalScope()) {
			return false;
		}
		if (this.fromScope != other.fromScope) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "UpgradeScope[" + this.fromScope
		+ " -> " + getFinalScope() + ']';
	}

}
