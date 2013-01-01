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

import static org.o42a.core.ir.local.InlineControl.inlineControl;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.st.DefValue.defValue;

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
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


public final class ImperativeDefiner extends Definer {

	private static final ID OWNER_ID = ID.id("owner");

	private final PrefixPath localPrefix;
	private final Command command;

	public ImperativeDefiner(ImperativeBlock block, DefinerEnv env) {
		super(block, env);

		final LocalScope localScope = block.getScope();
		final Scope ownerScope = localScope.getEnclosingScope();

		this.localPrefix =
				localScope.toMember()
				.getMemberKey()
				.toPath()
				.toPrefix(ownerScope);
		this.command = block.command(new ImperativeDefinerCommandEnv(this, env));
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
	public DefTarget toTarget(Scope origin) {

		final Scope localScope = getLocalPrefix().rescope(origin);
		final DefTarget target = getCommand().toTarget(localScope);

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
	public TypeParameters<?> typeParameters(Scope scope) {

		final Scope localScope = getLocalPrefix().rescope(scope);
		final TypeParameters<?> typeParameters =
				getCommand().typeParameters(localScope);

		if (typeParameters == null) {
			return null;
		}

		return typeParameters.prefixWith(getLocalPrefix());
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

			final Value<?> value = initialValue.getValue();

			if (value != null) {
				return defValue(value.prefixWith(getLocalPrefix()));
			}

			return initialValue.getCondition().toDefValue();
		}

		return initialValue.getCondition().toDefValue();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {

		final Scope localScope = getLocalPrefix().rescope(origin);

		getCommand().resolveTargets(resolver, localScope);
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
	public Eval eval(CodeBuilder builder, Scope origin) {
		assert getStatement().assertFullyResolved();
		return new LocalEval(getBlock(), getCommand());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {

		final LocalScope local =
				getLocalPrefix().rescope(resolver.getScope()).toLocal();

		getCommand().resolveAll(
				local.walkingResolver(resolver.getResolver())
				.fullResolver(resolver, resolver.getRefUsage()));
	}

	private static final class InlineLocal extends InlineEval {

		private final InlineCmd cmd;

		InlineLocal(InlineCmd cmd) {
			super(null);
			this.cmd = cmd;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final InlineControl control = inlineControl(dirs);

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

	private static final class LocalEval implements Eval {

		private final ImperativeBlock block;
		private final Command command;

		LocalEval(ImperativeBlock block, Command command) {
			this.block = block;
			this.command = command;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ObjectOp ownerObject = host.materialize(
					dirs.dirs(),
					tempObjHolder(dirs.getAllocator()));
			final LocalScope scope = getBlock().getScope().toLocal();
			final Obj ownerType = scope.getOwner();
			final ObjOp ownerBody =
					ownerObject.cast(OWNER_ID, dirs.dirs(), ownerType);
			final LocalIR ir = scope.ir(host.getGenerator());

			ir.write(dirs, ownerBody, null, this.command);
		}

		@Override
		public String toString() {
			if (this.command == null) {
				return super.toString();
			}
			return this.command.toString();
		}

		private final ImperativeBlock getBlock() {
			return this.block;
		}

	}

}
