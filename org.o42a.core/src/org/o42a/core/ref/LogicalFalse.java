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
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.use.UserInfo;


final class LogicalFalse extends Logical {

	LogicalFalse(LocationInfo location, Scope scope) {
		super(location, scope);
	}

	@Override
	public LogicalValue getConstantValue() {
		return LogicalValue.FALSE;
	}

	@Override
	public LogicalValue logicalValue(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return LogicalValue.FALSE;
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new LogicalFalse(this, reproducer.getScope());
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final Code code = dirs.code();

		code.debug("Logical: FALSE");
		dirs.goWhenFalse(code);
	}

	@Override
	public String toString() {
		return "FALSE";
	}

	@Override
	protected void fullyResolve(UserInfo user) {
	}

}
