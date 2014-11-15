/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


final class AssignmentCommand extends Command {

	AssignmentCommand(AssignmentStatement statement, CommandEnv env) {
		super(statement, env);
	}

	public final AssignmentStatement getAssignment() {
		return (AssignmentStatement) getStatement();
	}

	public final AssignmentKind getAssignmentKind() {
		return getAssignment().getAssignmentKind();
	}

	@Override
	public CommandTargets getTargets() {
		return actionCommand();
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return null;
	}

	@Override
	public Action action(Resolver resolver) {
		return getAssignmentKind().action(resolver);
	}

	@Override
	public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
		return getAssignmentKind().escapeFlag(analyzer, scope);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return getAssignmentKind().inline(normalizer, origin);
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return getAssignmentKind().normalize(normalizer, origin);
	}

	@Override
	public Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return getAssignmentKind().cmd();
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getAssignmentKind().resolve(resolver);
	}

}
