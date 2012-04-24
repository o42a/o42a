/*
    Compiler Core
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
package org.o42a.core.st.impl.imperative;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExitLoop;
import org.o42a.core.st.action.RepeatLoop;


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
	public DefTarget toTarget() {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public Action initialCond(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	protected void fullyResolve(LocalResolver resolver) {
	}

	static class ExitCommand extends EllipsisCommand {

		ExitCommand(EllipsisStatement ellipsis, CommandEnv env) {
			super(ellipsis, env);
		}

		@Override
		public CommandTargets getCommandTargets() {
			return exitCommand(this);
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			return new ExitLoop(this, getEllipsis().getName());
		}

		@Override
		public Cmd cmd() {
			assert getStatement().assertFullyResolved();
			return new ExitCmd(getEllipsis());
		}

	}

	static class RepeatCommand extends EllipsisCommand {

		RepeatCommand(EllipsisStatement ellipsis, CommandEnv env) {
			super(ellipsis, env);
		}

		@Override
		public CommandTargets getCommandTargets() {
			return repeatCommand();
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			return new RepeatLoop(this, getEllipsis().getName());
		}

		@Override
		public Cmd cmd() {
			assert getStatement().assertFullyResolved();
			return new RepeatCmd(getEllipsis());
		}

	}

	private static final class ExitCmd extends Cmd {

		ExitCmd(EllipsisStatement statement) {
			super(statement);
		}

		@Override
		public void write(Control control) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.exitBraces(st, st.getName());
		}

	}

	private static final class RepeatCmd extends Cmd {

		RepeatCmd(EllipsisStatement statement) {
			super(statement);
		}

		@Override
		public void write(Control control) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.repeat(st, st.getName());
		}

	}

}
