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

import static org.o42a.core.def.LogicalDef.trueLogicalDef;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


class LocalDef extends ValueDef {

	private final ImperativeBlock block;
	private final boolean explicit;
	private Logical logical;

	LocalDef(
			ImperativeBlock block,
			Rescoper rescoper,
			boolean explicit) {
		super(
				sourceOf(block),
				block,
				trueLogicalDef(block, block.getScope()).rescope(rescoper),
				rescoper);
		this.block = block;
		this.explicit = explicit;
	}

	private LocalDef(
			LocalDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper,
			Logical logical) {
		super(prototype, prerequisite, rescoper);
		this.block = prototype.block;
		this.explicit = prototype.explicit;
		this.logical = logical;
	}

	public final ImperativeBlock getBlock() {
		return this.block;
	}

	@Override
	public DefKind getKind() {
		return DefKind.PROPOSITION;
	}

	@Override
	public ValueType<?> getValueType() {
		return getBlock().getValueType();
	}

	@Override
	public LocalDef and(Logical logical) {
		if (logical == null || logical.isTrue()) {
			return this;
		}

		final Logical oldLogical = logical();
		final Logical newLogical = oldLogical.and(logical);

		if (oldLogical == newLogical) {
			return this;
		}

		return new LocalDef(
				this,
				getPrerequisite(),
				getRescoper(),
				newLogical);
	}

	@Override
	public void writeValue(Code code, CodePos exit, HostOp host, ValOp result) {

		final HostOp rescopedHost = getRescoper().rescope(code, exit, host);
		final ObjectOp ownerObject = rescopedHost.toObject(code, exit);

		assert ownerObject != null :
			"Local scope owner expected: " + rescopedHost;

		final LocalScope scope = getBlock().getScope().toLocal();
		final Obj ownerType = scope.getOwner();
		final ObjOp ownerBody = ownerObject.cast(code, exit, ownerType);
		final LocalIRBase ir = scope.ir(host.getGenerator());

		if (this.explicit) {
			ir.writeValue(code, exit, result, ownerBody, null);
		} else {
			ir.writeValue(
					code,
					exit,
					result,
					ownerType.ir(host.getGenerator())
					.op(host.getBuilder(), code),
					ownerBody);
		}
	}

	@Override
	protected LogicalDef buildPrerequisite() {
		return trueLogicalDef(this, getScope());
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final LocalScope local = scope.toLocal();

		assert local != null :
			"Not a local scope: " + scope;

		return getBlock().initialValue(local).getValue();
	}

	@Override
	protected Logical logical() {
		if (this.logical != null) {
			return this.logical;
		}
		return this.logical = new LocalLogical(this);
	}

	@Override
	protected LocalDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite) {
		return new LocalDef(this, prerequisite, rescoper, this.logical);
	}

	private static final class LocalLogical extends Logical {

		private final LocalDef def;

		LocalLogical(LocalDef def) {
			super(def, def.getScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.RUNTIME;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {

			final LocalScope local =
				this.def.getRescoper().rescope(scope).toLocal();

			assert local != null :
				"Not a local scope: " + scope;

			final Action action =
				this.def.getBlock().initialLogicalValue(local);

			return action.getLogicalValue();
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Logical: " + this);

			final ValOp result = code.allocate(VAL_TYPE).storeUnknown(code);

			this.def.writeValue(code, exit, host, result);
		}

		@Override
		public String toString() {
			return this.def + "?";
		}

	}

}
