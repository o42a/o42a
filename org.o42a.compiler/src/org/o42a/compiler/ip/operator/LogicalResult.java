/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import org.o42a.common.object.BuiltinObject;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


final class LogicalResult extends BuiltinObject {

	private final LogicalExpression ref;
	private Ref operand;

	LogicalResult(LogicalExpression ref) {
		super(ref, ref.distributeIn(ref.getContainer()), ValueStruct.VOID);
		this.ref = ref;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> value = operand().value(resolver);

		if (!value.getKnowledge().isKnown()) {
			return ValueType.VOID.runtimeValue();
		}

		switch (this.ref.getNode().getOperator()) {
		case NOT:
			if (value.getKnowledge().isFalse()) {
				return Value.voidValue();
			}
			return Value.falseValue();
		case IS_TRUE:
			if (value.getKnowledge().isFalse()) {
				return Value.falseValue();
			}
			return Value.voidValue();
		case KNOWN:
			if (value.getKnowledge().hasUnknownCondition()) {
				return Value.falseValue();
			}
			return Value.voidValue();
		case UNKNOWN:
			if (value.getKnowledge().hasUnknownCondition()) {
				return Value.voidValue();
			}
			return Value.falseValue();
		default:
			throw new IllegalStateException(
					"Unsupported logical operator: "
					+ this.ref.getNode().getOperator().getSign());
		}
	}

	@Override
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue operand = operand().inline(normalizer, origin);

		if (operand == null) {
			return null;
		}

		return new Inline(valueStruct, this.ref, operand);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return this.ref.write(dirs, host, operand().op(host), null);
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		operand().resolve(resolver).resolveValue();
	}

	@Override
	public String toString() {
		return this.ref != null ? this.ref.toString() : "LogicalOp";
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {
		return this.ref.resolve(enclosing);
	}

	private final Ref operand() {
		if (this.operand != null) {
			return this.operand;
		}
		return this.operand = this.ref.operand().rescope(getScope());
	}

	private static final class Inline extends InlineValue {

		private final LogicalExpression ref;
		private final InlineValue operandValue;

		Inline(
				ValueStruct<?, ?> valueStruct,
				LogicalExpression ref,
				InlineValue operandValue) {
			super(null, valueStruct);
			this.ref = ref;
			this.operandValue = operandValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return this.ref.write(dirs, host, null, this.operandValue);
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
