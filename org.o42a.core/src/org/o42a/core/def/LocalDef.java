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

	public final ImperativeBlock getBlock() {
		return this.block;
	}

	@Override
	public ValueType<?> getValueType() {
		return getBlock().getValueType();
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
	protected Logical buildPrerequisite() {
		return logicalTrue(this, getOwnerScope());
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final LocalScope local = this.localRescoper.rescope(scope).toLocal();

		assert local != null :
			"Not a local scope: " + scope;

		return getBlock().initialValue(local).getValue();
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
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);

			final LocalScope local =
				this.def.localRescoper.rescope(scope).toLocal();

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
