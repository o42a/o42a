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

import static org.o42a.core.ir.local.Control.mainControl;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;


public final class ImperativeDefiner extends Definer {

	private final Command command;

	public ImperativeDefiner(ImperativeBlock block, CommandEnv env) {
		super(block, env);
		this.command =
				block.command(new ImperativeDefinerCommandEnv(env));
	}

	public final ImperativeBlock getBlock() {
		return (ImperativeBlock) getStatement();
	}

	public final Command getCommand() {
		return this.command;
	}

	@Override
	public CommandTargets getTargets() {
		return this.command.getTargets();
	}

	@Override
	public DefTarget toTarget(Scope origin) {

		final DefTarget target = getCommand().toTarget(origin);

		if (target == null) {
			return null;
		}

		final Ref ref = target.getRef();

		if (ref == null) {
			return target;
		}

		return new DefTarget(ref);
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return getCommand().typeParameters(scope);
	}

	@Override
	public Action action(Resolver resolver) {
		return getCommand().action(resolver);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		getCommand().resolveTargets(resolver, origin);
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {

		final InlineCmd inline = getCommand().inlineCmd(normalizer, origin);

		if (inline == null) {
			return null;
		}

		return new InlineImperative(inline);
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {

		final InlineCmd inline = getCommand().normalizeCmd(normalizer, origin);

		if (inline == null) {
			return null;
		}

		return new InlineImperative(inline);
	}

	@Override
	public Eval eval(CodeBuilder builder, Scope origin) {
		assert getStatement().assertFullyResolved();
		return new ImperativeEval(getCommand());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getCommand().resolveAll(resolver);
	}

	private static final class InlineImperative extends InlineEval {

		private final InlineCmd cmd;

		InlineImperative(InlineCmd cmd) {
			super(null);
			this.cmd = cmd;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Control control = mainControl(dirs);

			this.cmd.write(control);

			control.end();
		}

		@Override
		public String toString() {
			if (this.cmd == null) {
				return super.toString();
			}
			return this.cmd.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class ImperativeEval implements Eval {

		private final Command command;

		ImperativeEval(Command command) {
			this.command = command;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Control control = mainControl(dirs);

			this.command.cmd().write(control);

			control.end();
		}

		@Override
		public String toString() {
			if (this.command == null) {
				return super.toString();
			}
			return this.command.toString();
		}

	}

}
