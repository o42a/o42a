/*
    Compiler Core
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
package org.o42a.core.member;

import static org.o42a.core.ir.cmd.Cmds.noCmd;
import static org.o42a.core.ir.cmd.Cmds.noInlineCmd;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.Condition;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


public abstract class DeclarationCommand extends Command {

	public DeclarationCommand(DeclarationStatement statement, CommandEnv env) {
		super(statement, env);
	}

	public final DeclarationStatement getDeclarationStatement() {
		return (DeclarationStatement) getStatement();
	}

	@Override
	public final DefTarget toTarget(Scope origin) {
		return null;
	}

	@Override
	public final TypeParameters<?> typeParameters(Scope scope) {
		return null;
	}

	@Override
	public final Action action(Resolver resolver) {
		return new ExecuteCommand(this, Condition.TRUE);
	}

	@Override
	public final void resolveTargets(TargetResolver resolver, Scope origin) {
	}

	@Override
	public final InlineCmd<?> inline(Normalizer normalizer, Scope origin) {
		return noInlineCmd();
	}

	@Override
	public final InlineCmd<?> normalize(
			RootNormalizer normalizer,
			Scope origin) {
		return null;
	}

	@Override
	public final Cmd<?> cmd(Scope origin) {
		return noCmd();
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	protected final void fullyResolve(FullResolver resolver) {
	}

}
