/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.def.impl;

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public class LocalDef extends ValueDef {

	public static ValueDef localDef(
			ImperativeBlock block,
			Scope scope,
			Definer definer) {

		final Obj actualOwner = scope.toObject();
		final Obj explicitOwner = block.getScope().getOwner();

		assert actualOwner == explicitOwner :
			"LocalDef can only be constructed for explicit local scope";

		return new LocalDef(scope, block, definer);
	}

	private final Scope ownerScope;
	private final ImperativeBlock block;
	private final Definer definer;
	private final PrefixPath localPrefix;

	private LocalDef(
			Scope ownerScope,
			ImperativeBlock block,
			Definer definer) {
		super(sourceOf(block), block, noScopeUpgrade(ownerScope));
		this.ownerScope = ownerScope;
		this.block = block;
		this.definer = definer;
		this.localPrefix = getOwnerScope().pathTo(block.getScope());
	}

	private LocalDef(LocalDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.ownerScope = prototype.ownerScope;
		this.block = prototype.block;
		this.definer = prototype.definer;
		this.localPrefix = prototype.localPrefix;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	public final ImperativeBlock getBlock() {
		return this.block;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {

		final Scope scope = this.localPrefix.rescope(getScope());

		assert scope.toLocal() != null :
			"Not a local scope: " + scope;

		final ValueStruct<?, ?> valueStruct = this.definer.valueStruct(scope);

		if (valueStruct == null) {
			return null;
		}

		return valueStruct
				.prefixWith(this.localPrefix)
				.prefixWith(getScopeUpgrade().toPrefix());
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("LocalDef{");
		if (hasPrerequisite()) {
			out.append(getPrerequisite()).append("? ");
		}

		final Logical precondition = getPrecondition();

		if (!precondition.isTrue()) {
			out.append(precondition).append(", ");
		}
		out.append(getLocation());
		if (isClaim()) {
			out.append("!}");
		} else {
			out.append(".}");
		}

		return out.toString();
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, getOwnerScope());
	}

	@Override
	protected boolean hasConstantValue() {
		return false;
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {

		final LocalScope local =
				this.localPrefix.rescope(resolver.getScope()).toLocal();

		assert local != null :
			"Not a local scope: " + resolver;

		return this.definer.initialValue(
				local.walkingResolver(resolver)).getValue();
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, getOwnerScope());
	}

	@Override
	protected Logical buildLogical() {
		return new LocalLogical(this);
	}

	@Override
	protected LocalDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new LocalDef(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {

		final LocalScope local =
				this.localPrefix.rescope(resolver.getScope()).toLocal();

		getBlock().resolveImperative(local.walkingResolver(resolver));
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	protected void normalizeDef(Normalizer normalizer) {
		// TODO Implement imperative block normalization.
	}

	@Override
	protected ValOp writeDefValue(ValDirs dirs, HostOp host) {
		// Imperative block`s value CAN be UNKNOWN.
		return writeValue(dirs, host);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final ObjectOp ownerObject = host.materialize(dirs.dirs());
		final LocalScope scope = getBlock().getScope().toLocal();
		final Obj ownerType = scope.getOwner();
		final ObjOp ownerBody =
				ownerObject.cast(dirs.id("owner"), dirs.dirs(), ownerType);
		final LocalIR ir = scope.ir(host.getGenerator());

		return ir.writeValue(dirs, ownerBody, null);
	}

	private Scope getOwnerScope() {
		return this.ownerScope;
	}

	private static final class LocalLogical extends Logical {

		private final LocalDef def;

		LocalLogical(LocalDef def) {
			super(def, def.getOwnerScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.RUNTIME;
		}

		@Override
		public LogicalValue logicalValue(Resolver resolver) {
			assertCompatible(resolver.getScope());

			final Resolver localResolver =
					this.def.localPrefix.rescope(resolver);
			final LocalScope local = localResolver.getScope().toLocal();

			assert local != null :
				"Not a local scope: " + resolver;

			final Action action = this.def.definer.initialLogicalValue(
					local.walkingResolver(resolver));

			return action.getLogicalValue();
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public InlineCond inline(Normalizer normalizer, Scope origin) {
			return null;
		}

		@Override
		public void write(CodeDirs dirs, HostOp host) {
			assert assertFullyResolved();

			final CodeDirs subDirs =
					dirs.begin("local_logical", "Local logical: " + this);

			final Obj owner = this.def.getOwnerScope().toObject();
			final ValueStruct<?, ?> valueStruct =
					owner.value().getValueStruct();
			final ValDirs valDirs = subDirs.value(valueStruct, "local_val");

			this.def.writeValue(valDirs, host);
			valDirs.done();

			subDirs.end();
		}

		@Override
		public String toString() {
			return this.def + "?";
		}

		@Override
		protected void fullyResolve(Resolver resolver) {
			this.def.resolveAll(resolver);
		}

	}

}
