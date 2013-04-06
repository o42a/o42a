/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.op.CodeDirs;


final class VariableAssignmentCmd implements Cmd {

	private final VariableAssignment assignment;

	VariableAssignmentCmd(VariableAssignment assignment) {
		this.assignment = assignment;
	}

	@Override
	public void write(Control control) {

		final CodeDirs dirs = control.getBuilder().dirs(
				control.code(),
				control.falseDir());
		final CodeDirs subDirs =
				dirs.begin("assign", this.assignment.toString());

		final HostOp destination =
				this.assignment.getStatement().getDestination()
				.op(control.host())
				.target(subDirs);
		final HostOp value =
				this.assignment.getValue().op(control.host()).target(subDirs);

		destination.value().assign(subDirs, value);

		subDirs.done();
		dirs.done();
	}

	@Override
	public String toString() {
		if (this.assignment == null) {
			return super.toString();
		}
		return this.assignment.toString();
	}

}
