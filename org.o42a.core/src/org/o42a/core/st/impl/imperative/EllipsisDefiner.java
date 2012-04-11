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

import static org.o42a.core.st.CommandTarget.actionCommand;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.Control;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExitLoop;
import org.o42a.core.st.action.RepeatLoop;
import org.o42a.core.value.ValueStruct;


abstract class EllipsisDefiner extends AbstractCommand {

	EllipsisDefiner(EllipsisStatement ellipsis, CommandEnv env) {
		super(ellipsis, env);
	}

	public final EllipsisStatement getEllipsis() {
		return (EllipsisStatement) getStatement();
	}

	@Override
	public final CommandTarget getCommandTarget() {
		return actionCommand(getStatement());
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return null;
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InlineCmd inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	protected void fullyResolve(LocalResolver resolver) {
	}

	static class ExitDefiner extends EllipsisDefiner {

		ExitDefiner(EllipsisStatement ellipsis, CommandEnv env) {
			super(ellipsis, env);
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			return new ExitLoop(this, getEllipsis().getName());
		}

		@Override
		protected Cmd createCmd(CodeBuilder builder) {
			return new ExitCmd(builder, getEllipsis());
		}

	}

	static class RepeatDefiner extends EllipsisDefiner {

		RepeatDefiner(EllipsisStatement ellipsis, CommandEnv env) {
			super(ellipsis, env);
		}

		@Override
		public Action initialValue(LocalResolver resolver) {
			return new RepeatLoop(this, getEllipsis().getName());
		}

		@Override
		protected Cmd createCmd(CodeBuilder builder) {
			return new RepeatCmd(builder, getEllipsis());
		}

	}

	private static final class ExitCmd extends Cmd {

		ExitCmd(CodeBuilder builder, EllipsisStatement statement) {
			super(builder, statement);
		}

		@Override
		public void write(Control control) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.exitBraces(st, st.getName());
		}

	}

	private static final class RepeatCmd extends Cmd {

		RepeatCmd(CodeBuilder builder, EllipsisStatement statement) {
			super(builder, statement);
		}

		@Override
		public void write(Control control) {

			final EllipsisStatement st = (EllipsisStatement) getStatement();

			control.repeat(st, st.getName());
		}

	}

}
