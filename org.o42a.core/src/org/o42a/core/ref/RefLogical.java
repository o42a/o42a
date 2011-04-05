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
package org.o42a.core.ref;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class RefLogical extends Logical {

	private final Ref ref;

	RefLogical(Ref ref) {
		super(ref, ref.getScope());
		this.ref = ref;
	}

	@Override
	public LogicalValue getConstantValue() {
		if (this.ref.isKnownStatic()) {
			return this.ref.getValue().getLogicalValue();
		}
		return LogicalValue.RUNTIME;
	}

	@Override
	public LogicalValue logicalValue(Scope scope) {
		assertCompatible(scope);
		return this.ref.value(scope).getLogicalValue();
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {

		final Ref reproduced = this.ref.reproduce(reproducer);

		if (reproduced == null) {
			return null;
		}

		return reproduced.getLogical();
	}

	@Override
	public void write(Code code, CodePos exit, HostOp host) {
		code.begin("Logical of ref " + this);
		exit = code.end("debug_ref_logical_exit", exit);
		this.ref.op(host).writeLogicalValue(code, exit);
		code.end();
	}

	@Override
	public String toString() {
		return "(" + this.ref + "?)";
	}

}