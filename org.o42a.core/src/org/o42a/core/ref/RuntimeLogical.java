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

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class RuntimeLogical extends Logical {

	RuntimeLogical(LocationInfo location, Scope scope) {
		super(location, scope);
	}

	@Override
	public LogicalValue getConstantValue() {
		return LogicalValue.RUNTIME;
	}

	@Override
	public LogicalValue logicalValue(Scope scope) {
		assertCompatible(scope);
		return LogicalValue.RUNTIME;
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new RuntimeLogical(this, reproducer.getScope());
	}

	@Override
	public void resolveAll() {
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		throw new UnsupportedOperationException(
				"Abstract run-time logical should not generate any code");
	}

	@Override
	public String toString() {
		return "RUN-TIME";
	}

}
