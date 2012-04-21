/*
    Compiler Core
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
package org.o42a.core.ref.impl.cond;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.Directive;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


final class RefConditionCommand extends Command {

	private final RefCommand refCommand;

	RefConditionCommand(RefCondition ref, CommandEnv env) {
		super(ref, env);
		this.refCommand = ref.getRef().command(new Env(env));
	}

	public final Ref getRef() {
		return ((RefCondition) getStatement()).getRef();
	}

	public final RefCommand getRefCommand() {
		return this.refCommand;
	}

	@Override
	public CommandTargets getCommandTargets() {
		if (!getRef().isConstant()) {
			return actionCommand();
		}
		return actionCommand().setConstant();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {

		final Directive directive = getRef().resolve(resolver).toDirective();

		if (directive == null) {
			return null;
		}

		return new ApplyDirective(getRef(), resolver, directive);
	}

	@Override
	public DefTarget toTarget() {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return getRefCommand().initialCond(resolver);
	}

	@Override
	public Action initialCond(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {

		final InlineValue value = getRef().inline(normalizer, origin);

		if (value != null) {
			return new Inline(value);
		}

		getRef().normalize(normalizer.getAnalyzer());

		return null;
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	protected void fullyResolve(LocalResolver resolver) {
		getRef().resolve(resolver).resolveLogical();
	}

	@Override
	protected Cmd createCmd(CodeBuilder builder) {
		return new CondCmd(builder, getRef(), getRefCommand());
	}

	private final static class Env extends CommandEnv {

		Env(CommandEnv initialEnv) {
			super(initialEnv.getStatements());
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return null;// To prevent Ref adaption.
		}

	}

	private static final class Inline extends InlineCmd {

		private final InlineValue value;

		Inline(InlineValue value) {
			super(null);
			this.value = value;
		}

		@Override
		public void write(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			this.value.writeCond(dirs, control.host());
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return "(++" + this.value + ")";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class CondCmd extends Cmd {

		private final RefCmd refCmd;

		CondCmd(CodeBuilder builder, Ref ref, RefCommand refCommand) {
			super(builder, ref);
			this.refCmd = refCommand.cmd(builder);
		}

		@Override
		public void write(Control control) {
			this.refCmd.writeCond(control);
		}

	}

}
