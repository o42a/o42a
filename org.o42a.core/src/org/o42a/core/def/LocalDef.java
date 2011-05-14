/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.def;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


class LocalDef extends ValueDef {

	private final Scope ownerScope;
	private final ImperativeBlock block;
	private final boolean explicit;
	private final Rescoper localRescoper;

	LocalDef(
			Scope ownerScope,
			ImperativeBlock block,
			boolean explicit) {
		super(sourceOf(block), block, transparentRescoper(ownerScope));
		this.ownerScope = ownerScope;
		this.block = block;
		this.explicit = explicit;
		this.localRescoper = block.getScope().rescoperTo(getOwnerScope());
	}

	private LocalDef(LocalDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.ownerScope = prototype.ownerScope;
		this.block = prototype.block;
		this.explicit = prototype.explicit;
		this.localRescoper = prototype.localRescoper;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	public final ImperativeBlock getBlock() {
		return this.block;
	}

	@Override
	public ValueType<?> getValueType() {
		return getBlock().getValueType();
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
	protected Value<?> calculateValue(Resolver resolver) {

		final LocalScope local =
			this.localRescoper.rescope(resolver).getScope().toLocal();

		assert local != null :
			"Not a local scope: " + resolver;

		return getBlock().initialValue(local.newResolver(resolver)).getValue();
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
	protected LocalDef create(Rescoper rescoper, Rescoper additionalRescoper) {
		return new LocalDef(this, rescoper);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {

		final Resolver localResolver = this.localRescoper.rescope(resolver);

		getBlock().resolveValues(localResolver);
	}

	@Override
	protected void writeDef(CodeDirs dirs, ValOp result, HostOp host) {
		// Imperative block`s value CAN be UNKNOWN.
		writeValue(dirs, result, host);
	}

	@Override
	protected void writeValue(CodeDirs dirs, ValOp result, HostOp host) {
		assert assertFullyResolved();

		final Code code = dirs.code();
		final ObjectOp ownerObject = host.toObject(dirs);

		assert ownerObject != null :
			"Local scope owner expected: " + host;

		final LocalScope scope = getBlock().getScope().toLocal();
		final Obj ownerType = scope.getOwner();
		final ObjOp ownerBody =
			ownerObject.cast(dirs.id("owner"), dirs, ownerType);
		final LocalIRBase ir = scope.ir(host.getGenerator());

		if (this.explicit) {
			ir.writeValue(code, result, ownerBody, null);
		} else {
			ir.writeValue(
					code,
					result,
					ownerType.ir(host.getGenerator())
					.op(host.getBuilder(), dirs.code()),
					ownerBody);
		}

		result.go(code, dirs);
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

			final LocalScope local =
				this.def.localRescoper.rescope(resolver).getScope().toLocal();

			assert local != null :
				"Not a local scope: " + resolver;

			final Action action = this.def.getBlock().initialLogicalValue(
					local.newResolver(resolver));

			return action.getLogicalValue();
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(CodeDirs dirs, HostOp host) {
			assert assertFullyResolved();
			dirs = dirs.begin("local_logical", "Local logical: " + this);

			final Code code = dirs.code();
			final ValOp result =
				code.allocate(null, VAL_TYPE).storeIndefinite(code);

			this.def.writeValue(dirs, result, host);

			dirs.end();
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
