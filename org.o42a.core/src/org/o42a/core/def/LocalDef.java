/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.core.def.CondDef.trueCondDef;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ActionVisitor;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


class LocalDef extends Def {

	private static final ActionValueVisitor ACTION_VALUE_VISITOR =
		new ActionValueVisitor();

	private final boolean explicit;
	private Cond condition;

	LocalDef(
			ImperativeBlock block,
			CondDef prerequisite,
			Rescoper rescoper,
			boolean explicit) {
		super(
				sourceOf(block),
				block,
				prerequisite,
				rescoper);
		this.explicit = explicit;
	}

	private LocalDef(
			LocalDef prototype,
			CondDef prerequisite,
			Rescoper rescoper,
			Cond condition) {
		super(prototype, prerequisite, rescoper);
		this.explicit = prototype.explicit;
		this.condition = condition;
	}

	public final ImperativeBlock getBlock() {
		return (ImperativeBlock) getScoped();
	}

	@Override
	public boolean isClaim() {
		return false;
	}

	@Override
	public ValueType<?> getValueType() {
		return getBlock().getValueType();
	}

	@Override
	public LocalDef and(Cond condition) {
		if (condition == null || condition.isTrue()) {
			return this;
		}

		final Cond oldCondition = condition();
		final Cond newCondition = oldCondition.and(condition);

		if (oldCondition == newCondition) {
			return this;
		}

		return new LocalDef(
				this,
				getPrerequisite(),
				getRescoper(),
				newCondition);
	}

	@Override
	public LocalDef reproduce(Reproducer reproducer) {
		return (LocalDef) super.reproduce(reproducer);
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
	protected CondDef buildPrerequisite() {
		return trueCondDef(this, getScope());
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final LocalScope local = scope.toLocal();

		assert local != null :
			"Not a local scope: " + scope;

		final Action action = getBlock().initialValue(local);

		return action.accept(ACTION_VALUE_VISITOR);
	}

	@Override
	protected Cond condition() {
		if (this.condition != null) {
			return this.condition;
		}
		return this.condition = new LocalCondition(this);
	}

	@Override
	protected Def create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			CondDef prerequisite) {
		return new LocalDef(this, prerequisite, rescoper, this.condition);
	}

	private static final class ActionValueVisitor
			extends ActionVisitor<Void, Value<?>> {

		@Override
		public Value<?> visitReturnValue(
				ReturnValue returnValue,
				Void p) {
			return returnValue.getResult();
		}

		@Override
		protected Value<?> visitAction(Action action, Void p) {
			switch (action.getLogicalValue()) {
			case TRUE:
				return Value.voidValue();
			case FALSE:
				return Value.falseValue();
			case RUNTIME:
				return ValueType.VOID.runtimeValue();
			}
			throw new IllegalArgumentException("Can not handle " + action);
		}

	}

	private static final class LocalCondition extends Cond {

		private final LocalDef def;

		LocalCondition(LocalDef def) {
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
				this.def.getBlock().initialCondition(local);

			return action.getLogicalValue();
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {

			final LocalDef def = this.def.reproduce(reproducer);

			if (def == null) {
				return null;
			}

			return def.condition();
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Cond: " + this);

			final ValOp result =
				code.allocate(host.getGenerator().valType()).storeUnknown(code);

			this.def.writeValue(code, exit, host, result);
		}

		@Override
		public String toString() {
			return this.def + "?";
		}

	}

}
