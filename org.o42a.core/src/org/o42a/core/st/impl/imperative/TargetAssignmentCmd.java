/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.imperative;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.link.LinkValueStruct;


final class TargetAssignmentCmd extends Cmd {

	TargetAssignmentCmd(CodeBuilder builder, AssignmentStatement assignment) {
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
				assignment.getDestination()
				.op(control.host())
				.target(subDirs);
		final ObjectOp link =
				assignment.getValue()
				.op(control.host())
				.target(subDirs)
				.materialize(subDirs);

		final LinkValueStruct linkStruct =
				link.value().getValueStruct().toLinkStruct();
		final ValDirs targetDirs = subDirs.value(linkStruct, "target");
		final Block code = targetDirs.code();
		final ValOp targetVal = link.value().writeValue(targetDirs);

		final DataOp targetPtr =
				targetVal.value(code.id("ptarget"), code)
				.toPtr(null, code)
				.load(null, code)
				.toData(null, code);
		final ObjectOp value = anonymousObject(
				getBuilder(),
				targetPtr,
				linkStruct.getTypeRef().typeObject(dummyUser()));

		destination.assign(subDirs, value);

		targetDirs.done();
		subDirs.end();
	}

}
