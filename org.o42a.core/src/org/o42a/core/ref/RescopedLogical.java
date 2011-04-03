/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ref;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class RescopedLogical extends Logical {

	private final Logical logical;
	private final Rescoper rescoper;

	RescopedLogical(Logical logical, Rescoper rescoper) {
		super(logical, rescoper.getFinalScope());
		this.logical = logical;
		this.rescoper = rescoper;
	}

	@Override
	public LogicalValue getConstantValue() {
		return this.logical.getConstantValue();
	}

	@Override
	public LogicalValue logicalValue(Scope scope) {
		return this.logical.logicalValue(this.rescoper.rescope(scope));
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		getLogger().notReproducible(this);
		return null;
	}

	@Override
	public void write(Code code, CodePos exit, HostOp host) {
		host = this.rescoper.rescope(code, exit, host);
		this.logical.write(code, exit, host);
	}

	@Override
	public Logical rescope(Rescoper rescoper) {

		final Rescoper oldRescoper = this.rescoper;

		if (rescoper.getFinalScope() == oldRescoper.getFinalScope()) {
			return this;
		}

		final Rescoper newRescoper = oldRescoper.and(rescoper);

		if (newRescoper.equals(oldRescoper)) {
			return this;
		}

		return new RescopedLogical(this.logical, newRescoper);
	}

	@Override
	public String toString() {
		return this.logical.toString();
	}

}
