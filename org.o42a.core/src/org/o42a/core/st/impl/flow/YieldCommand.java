/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.st.impl.flow;

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.Condition;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


final class YieldCommand extends Command {

	private final Ref value;

	public YieldCommand(YieldStatement statement, CommandEnv env) {
		super(statement, env);
		this.value = statement.getValue();
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
		return new ExecuteCommand(getStatement(), Condition.RUNTIME);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return NO_DEF_TARGET;
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Cmd cmd(Scope origin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		this.value.resolveAll(resolver.setRefUsage(TARGET_REF_USAGE));
	}

}
