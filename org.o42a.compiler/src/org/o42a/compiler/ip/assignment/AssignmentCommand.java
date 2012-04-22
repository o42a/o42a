/*
    Compiler
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
package org.o42a.compiler.ip.assignment;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.LogicalValue;


final class AssignmentCommand extends Command {

	private AssignmentKind assignmentKind;

	AssignmentCommand(AssignmentStatement statement, CommandEnv env) {
		super(statement, env);
	}

	public final AssignmentStatement getAssignment() {
		return (AssignmentStatement) getStatement();
	}

	public final AssignmentKind getAssignmentKind() {
		if (this.assignmentKind != null) {
			return this.assignmentKind;
		}

		this.assignmentKind = getAssignment().getAssignmentKind();
		this.assignmentKind.init(this);

		return this.assignmentKind;
	}

	@Override
	public CommandTargets getCommandTargets() {
		return actionCommand();
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return new ExecuteCommand(this, LogicalValue.RUNTIME);
	}

	@Override
	public Action initialCond(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget() {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return getAssignmentKind().inline(normalizer, origin);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		getAssignmentKind().normalize(normalizer);
	}

	@Override
	protected void fullyResolve(LocalResolver resolver) {
		getAssignmentKind().resolve(resolver);
	}

	@Override
	protected Cmd createCmd(CodeBuilder builder) {
		return getAssignmentKind().op(builder);
	}

}
