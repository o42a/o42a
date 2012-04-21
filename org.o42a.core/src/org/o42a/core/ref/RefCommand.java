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
package org.o42a.core.ref;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.RefEval;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.local.RefCmd;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public final class RefCommand extends Command {

	private ValueAdapter valueAdapter;

	RefCommand(Ref ref, CommandEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return (Ref) getStatement();
	}

	@Override
	public final CommandTargets getCommandTargets() {
		if (!getRef().isConstant()) {
			return returnCommand();
		}
		return returnCommand().setConstant();
	}

	public ValueAdapter getValueAdapter() {
		if (this.valueAdapter != null) {
			return this.valueAdapter;
		}

		final ValueStruct<?, ?> expectedStruct = env().getExpectedValueStruct();

		return this.valueAdapter = getRef().valueAdapter(expectedStruct, true);
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return new ReturnValue(
				this,
				resolver,
				getValueAdapter().value(resolver));
	}

	@Override
	public Action initialCond(LocalResolver resolver) {
		return new ExecuteCommand(
				this,
				getValueAdapter()
				.value(resolver)
				.getKnowledge()
				.toLogicalValue());
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {

		final InlineValue inline = getValueAdapter().inline(normalizer, origin);

		if (inline == null) {
			return null;
		}

		return new InlineRefCmd(inline);
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget() {

		final Ref target = getValueAdapter().toTarget();

		if (target == null) {
			return DefTarget.NO_DEF_TARGET;
		}

		return new DefTarget(target);
	}

	@Override
	public final RefCmd cmd(CodeBuilder builder) {
		return (RefCmd) super.cmd(builder);
	}

	@Override
	protected void fullyResolve(LocalResolver resolver) {
		getValueAdapter().resolveAll(resolver);
	}

	@Override
	protected final RefCmd createCmd(CodeBuilder builder) {
		return new RefCmdImpl(
				builder,
				getRef(),
				getValueAdapter().eval(builder));
	}

	private static final class RefCmdImpl extends RefCmd {

		private final RefEval eval;

		RefCmdImpl(CodeBuilder builder, Ref ref, RefEval eval) {
			super(builder, ref);
			this.eval = eval;
		}

		@Override
		public void writeCond(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			this.eval.writeCond(dirs, control.host());
		}

		@Override
		public void write(Control control) {

			final DefDirs dirs = control.defDirs();

			this.eval.write(dirs, control.host());

			dirs.done();
		}

	}

	private static final class InlineRefCmd extends InlineCmd {

		private final InlineValue inline;

		InlineRefCmd(InlineValue inline) {
			super(null);
			this.inline = inline;
		}

		@Override
		public void write(Control control) {

			final Block code = control.code();
			final ValDirs dirs =
					control.getBuilder().falseWhenUnknown(
							code,
							control.falseDir())
					.value(control.result());
			final ValOp value = this.inline.writeValue(dirs, control.host());

			dirs.done();

			control.returnValue(value);
		}

		@Override
		public String toString() {
			if (this.inline == null) {
				return super.toString();
			}
			return this.inline.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
