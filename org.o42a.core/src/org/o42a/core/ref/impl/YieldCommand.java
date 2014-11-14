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
package org.o42a.core.ref.impl;

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.object.meta.EscapeMode.ESCAPE_POSSIBLE;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.meta.EscapeMode;
import org.o42a.core.ref.*;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.Condition;


final class YieldCommand extends RefCommand {

	public YieldCommand(Ref ref, CommandEnv env) {
		super(ref, env);
	}

	@Override
	public CommandTargets getTargets() {
		return yieldCommand();
	}

	@Override
	public Action action(Resolver resolver) {
		return new ExecuteCommand(getStatement(), Condition.RUNTIME);
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return ESCAPE_POSSIBLE;
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return NO_DEF_TARGET;
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
	public String toString() {

		final Ref ref = getRef();

		if (ref == null) {
			return super.toString();
		}

		return "<<" + ref;
	}

	@Override
	protected Cmd refCmd(Eval value) {
		return new YieldCmd(value);
	}

}
