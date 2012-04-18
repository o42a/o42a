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
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.local.RefCmd;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public final class RefCommand extends Command {

	private ValueAdapter valueAdapter;
	private InlineValue inline;

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
				getValueAdapter().initialValue(resolver));
	}

	@Override
	public Action initialCond(LocalResolver resolver) {
		return new ExecuteCommand(
				this,
				getValueAdapter().initialCond(resolver));
	}

	@Override
	public InlineCmd inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue inline = getRef().inline(normalizer, origin);

		if (inline == null) {
			return null;
		}

		return new InlineRefCmd(inline);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.inline = getRef().inline(normalizer.newNormalizer(), getScope());
		if (this.inline == null) {
			getRef().normalize(normalizer.getAnalyzer());
		}
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public final RefCmd cmd(CodeBuilder builder) {
		return (RefCmd) super.cmd(builder);
	}

	@Override
	protected void fullyResolve(LocalResolver resolver) {
		getRef().resolve(resolver).resolveValue();
	}

	@Override
	protected final RefCmd createCmd(CodeBuilder builder) {
		if (this.inline == null) {
			return new RefCmdImpl(builder, getRef());
		}
		return new InlineRefCmdImpl(builder, getRef(), this.inline);
	}

	private static final class RefCmdImpl extends RefCmd {

		RefCmdImpl(CodeBuilder builder, Ref ref) {
			super(builder, ref);
		}

		@Override
		public void writeCond(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			getRef().op(control.host()).writeCond(dirs);

			dirs.end();
		}

		@Override
		public void write(Control control) {

			final Block code = control.code();
			final ValDirs dirs =
					control.getBuilder().falseWhenUnknown(
							code,
							control.falseDir())
					.value(control.result());
			final ValOp value = getRef().op(control.host()).writeValue(dirs);

			dirs.done();

			control.returnValue(value);
		}

	}

	private static final class InlineRefCmdImpl extends RefCmd {

		private final InlineValue inline;

		InlineRefCmdImpl(CodeBuilder builder, Ref ref, InlineValue inline) {
			super(builder, ref);
			this.inline = inline;
		}

		@Override
		public void writeCond(Control control) {

			final CodeDirs dirs = control.getBuilder().falseWhenUnknown(
					control.code(),
					control.falseDir());

			this.inline.writeCond(dirs, getBuilder().host());
		}

		@Override
		public void write(Control control) {

			final Block code = control.code();
			final ValDirs dirs =
					control.getBuilder().falseWhenUnknown(
							code,
							control.falseDir())
					.value(control.result());
			final ValOp value =
					this.inline.writeValue(dirs, getBuilder().host());

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
