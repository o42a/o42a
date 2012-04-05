/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.statement;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.op.CodeDirs;


final class AssignmentCmd extends Cmd {

	AssignmentCmd(CodeBuilder builder, AssignmentStatement assignment) {
		super(builder, assignment);
	}

	public final AssignmentStatement getAssignment() {
		return (AssignmentStatement) getStatement();
	}

	@Override
	public void write(Control control) {

		final AssignmentStatement assignment = getAssignment();
		final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
				control.code(),
				control.falseDir());
		final CodeDirs subDirs =
				dirs.begin("assign", assignment.toString());

		final HostOp destination =
				assignment.getDestination().op(control.host()).target(subDirs);
		final HostOp value =
				assignment.getValue().op(control.host()).target(subDirs);

		destination.assign(subDirs, value);

		subDirs.end();
	}

}
