/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.st;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.RootNormalizer;


public abstract class Definer extends Implication<Definer> {

	private Command command;

	public Definer(Statement statement, CommandEnv env) {
		super(statement, env);
	}

	public final Command getCommand() {
		if (this.command != null) {
			return this.command;
		}
		return this.command = getStatement().command(env());
	}

	@Override
	public InlineCmd inlineCmd(Normalizer normalizer, Scope origin) {
		return getCommand().inlineCmd(normalizer, origin);
	}

	@Override
	public InlineCmd normalizeCmd(RootNormalizer normalizer, Scope origin) {
		return getCommand().normalizeCmd(normalizer, origin);
	}

	@Override
	public Cmd cmd(Scope origin) {
		return getCommand().cmd(origin);
	}

	public abstract InlineEval inline(Normalizer normalizer, Scope origin);

	public abstract InlineEval normalize(
			RootNormalizer normalizer,
			Scope origin);

	public abstract Eval eval(Scope origin);

}
