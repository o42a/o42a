/*
    Compiler Core
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
package org.o42a.core.st.impl.imperative;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExitLoop;
import org.o42a.core.st.action.RepeatLoop;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


abstract class EllipsisCommand extends Command {

	EllipsisCommand(EllipsisStatement ellipsis, CommandEnv env) {
		super(ellipsis, env);
	}

	public final EllipsisStatement getEllipsis() {
		return (EllipsisStatement) getStatement();
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return null;
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
	protected void fullyResolve(FullResolver resolver) {
	}

	static class ExitCommand extends EllipsisCommand {

		ExitCommand(EllipsisStatement ellipsis, CommandEnv env) {
			super(ellipsis, env);
		}

		@Override
		public CommandTargets getTargets() {
			return exitCommand(getLocation());
		}

		@Override
		public Action action(Resolver resolver) {
			return new ExitLoop(this, getEllipsis().getName());
		}

		@Override
		public Cmd cmd(Scope origin) {
			assert getStatement().assertFullyResolved();
			return new ExitCmd(getEllipsis());
		}

	}

	static class RepeatCommand extends EllipsisCommand {

		RepeatCommand(EllipsisStatement ellipsis, CommandEnv env) {
			super(ellipsis, env);
		}

		@Override
		public CommandTargets getTargets() {
			return repeatCommand();
		}

		@Override
		public Action action(Resolver resolver) {
			return new RepeatLoop(this, getEllipsis().getName());
		}

		@Override
		public Cmd cmd(Scope origin) {
			assert getStatement().assertFullyResolved();
			return new RepeatCmd(getEllipsis());
		}

	}

	private static final class ExitCmd implements Cmd {

		private final EllipsisStatement statement;

		ExitCmd(EllipsisStatement statement) {
			this.statement = statement;
		}

		@Override
		public void write(Control control) {
			control.exitBraces(this.statement, this.statement.getName());
		}

		@Override
		public String toString() {
			if (this.statement == null) {
				return super.toString();
			}
			return this.statement.toString();
		}

	}

	private static final class RepeatCmd implements Cmd {

		private final EllipsisStatement statement;

		RepeatCmd(EllipsisStatement statement) {
			this.statement = statement;
		}

		@Override
		public void write(Control control) {
			control.repeat(this.statement, this.statement.getName());
		}

		@Override
		public String toString() {
			if (this.statement == null) {
				return super.toString();
			}
			return this.statement.toString();
		}

	}

}
