/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.st.assignment;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.op.CodeDirs;


final class VariableAssignmentEval implements Eval {

	private final VariableAssignment assignment;

	VariableAssignmentEval(VariableAssignment assignment) {
		this.assignment = assignment;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {

		final CodeDirs subDirs =
				dirs.dirs().begin("assign", this.assignment.toString());

		final HostOp destination =
				this.assignment.getStatement().getDestination()
				.op(host)
				.target(subDirs);
		final HostOp value =
				this.assignment.getValue().op(host).target(subDirs);

		destination.value().assign(subDirs, value);

		subDirs.done();
	}

	@Override
	public String toString() {
		if (this.assignment == null) {
			return super.toString();
		}
		return this.assignment.toString();
	}

}
