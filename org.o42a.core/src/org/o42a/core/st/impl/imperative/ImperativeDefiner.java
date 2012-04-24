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

import static org.o42a.core.ir.local.InlineControl.inlineControl;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.local.InlineControl;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.util.fn.Cancelable;


public final class ImperativeDefiner extends Definer {

	private final Command command;
	private final PrefixPath localPrefix;

	public ImperativeDefiner(ImperativeBlock block, DefinerEnv env) {
		super(block, env);
		this.command = block.command(new BlockCommandEnv(null, env));

		final LocalScope localScope = block.getScope();
		final Scope ownerScope = localScope.getEnclosingScope();

		this.localPrefix = ownerScope.pathTo(localScope);
	}

	public final ImperativeBlock getBlock() {
		return (ImperativeBlock) getStatement();
	}

	public final Command getCommand() {
		return this.command;
	}

	public final PrefixPath getLocalPrefix() {
		return this.localPrefix;
	}

	@Override
	public DefTargets getDefTargets() {
		return this.command.getCommandTargets().toDefTargets();
	}

	@Override
	public DefTarget toTarget() {

		final DefTarget target = getCommand().toTarget();

		if (target == null) {
			return null;
		}

		final Ref ref = target.getRef();

		if (ref == null) {
			return target;
		}

		return new DefTarget(ref.prefixWith(getLocalPrefix()));
	}

	@Override
	public DefValue value(Resolver resolver) {

		final LocalScope local =
				getLocalPrefix().rescope(resolver.getScope()).toLocal();

		assert local != null :
			"Not a local scope: " + resolver;

		final Action initialValue = getCommand()
				.initialValue(local.walkingResolver(resolver));

		if (initialValue.isAbort()) {
			return initialValue.getValue()
					.prefixWith(getLocalPrefix())
					.toDefValue();
		}

		return initialValue.getLogicalValue().toDefValue();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
		getCommand().resolveTargets(resolver);
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {

		final InlineCmd inline = getCommand().inline(
				normalizer,
				getLocalPrefix().rescope(origin));

		if (inline == null) {
			return null;
		}

		return new InlineLocal(inline);
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		if (origin != getBlock().getScope().getEnclosingScope()) {
			// Normalize only an explicit scope.
			return null;
		}

		final RootNormalizer localNormalizer = new RootNormalizer(
				normalizer.getAnalyzer(),
				getBlock().getScope());

		getCommand().normalize(localNormalizer);

		return null;
	}

	@Override
	public Eval eval(CodeBuilder builder) {
		assert getStatement().assertFullyResolved();
		return new LocalEval(builder, getBlock(), getCommand());
	}

	@Override
	protected void fullyResolve(Resolver resolver) {

		final LocalScope local =
				getLocalPrefix().rescope(resolver.getScope()).toLocal();

		getCommand().resolveAll(local.walkingResolver(resolver));
	}

	private static final class InlineLocal extends InlineEval {

		private final InlineCmd cmd;

		InlineLocal(InlineCmd cmd) {
			super(null);
			this.cmd = cmd;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final DefDirs localDirs = dirs.falseWhenUnknown();
			final InlineControl control = inlineControl(localDirs);

			this.cmd.write(control);

			control.end();
			localDirs.done();
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

	private static final class LocalEval extends Eval {

		private final Command command;

		LocalEval(
				CodeBuilder builder,
				ImperativeBlock block,
				Command command) {
			super(block);
			this.command = command;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ObjectOp ownerObject = host.materialize(dirs.dirs());
			final LocalScope scope = getBlock().getScope().toLocal();
			final Obj ownerType = scope.getOwner();
			final ObjOp ownerBody =
					ownerObject.cast(dirs.id("owner"), dirs.dirs(), ownerType);
			final Block localUnknown = dirs.addBlock("local_unknown");
			final ValDirs valDirs = dirs.valDirs().splitWhenUnknown(
					dirs.falseDir(),
					localUnknown.head());

			final LocalIR ir = scope.ir(host.getGenerator());
			final ValOp value = ir.writeValue(
					valDirs,
					ownerBody,
					null,
					this.command);

			valDirs.done();

			final Block code = dirs.code();

			if (code.exists()) {

				final Block hasLocal = code.addBlock("has_local");

				value.loadIndefinite(null, code)
				.goUnless(code, hasLocal.head());
				if (hasLocal.exists()) {
					dirs.returnValue(hasLocal, value);
				}
			}
			if (localUnknown.exists()) {
				localUnknown.go(code.tail());
			}
		}

		private final ImperativeBlock getBlock() {
			return (ImperativeBlock) getStatement();
		}

	}

}
