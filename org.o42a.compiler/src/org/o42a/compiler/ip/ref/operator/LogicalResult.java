/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.operator;

import org.o42a.ast.expression.UnaryOperator;
import org.o42a.common.builtin.BuiltinObject;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.meta.EscapeMode;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.util.fn.Cancelable;


final class LogicalResult extends BuiltinObject {

	private final LogicalExpression logical;
	private Ref operand;

	LogicalResult(LogicalExpression logical) {
		super(logical, logical.distribute(), ValueType.VOID);
		this.logical = logical;
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return operand().escapeMode(scope);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> value = operand().value(resolver);

		if (!value.getKnowledge().isKnown()) {
			return type().getParameters().runtimeValue();
		}

		final UnaryOperator operator = this.logical.getNode().getOperator();

		if (operator == UnaryOperator.NOT) {
			if (value.getKnowledge().isFalse()) {
				return ValueType.VOID.cast(type().getParameters())
						.compilerValue(Void.VOID);
			}
			return type().getParameters().falseValue();
		}
		if (operator == UnaryOperator.IS_TRUE) {
			if (value.getKnowledge().isFalse()) {
				return type().getParameters().falseValue();
			}
			return ValueType.VOID.cast(type().getParameters())
					.compilerValue(Void.VOID);
		}

		throw new IllegalStateException(
				"Unsupported logical operator: "
				+ this.logical.getNode().getOperator().getSign());
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		operand().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue operand = operand().inline(normalizer, origin);

		if (operand == null) {
			return null;
		}

		return new InlineLogical(this.logical, operand);
	}

	@Override
	public Eval evalBuiltin() {
		return new LogicalEval(this);
	}

	@Override
	public String toString() {
		if (this.logical == null) {
			return "LogicalOp";
		}
		return this.logical.toString();
	}

	@Override
	protected Nesting createNesting() {
		return this.logical.getNesting();
	}

	@Override
	protected Statefulness determineStatefulness() {
		return super.determineStatefulness()
				.setEager(this.logical.isEager());
	}

	private final Ref operand() {
		if (this.operand != null) {
			return this.operand;
		}
		return this.operand = this.logical.operand().rescope(getScope());
	}

	private static final class InlineLogical extends InlineEval {

		private final LogicalExpression ref;
		private final InlineValue operandValue;

		InlineLogical(LogicalExpression ref, InlineValue operandValue) {
			super(null);
			this.ref = ref;
			this.operandValue = operandValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.returnValue(this.ref.write(
					dirs.valDirs(),
					host,
					null,
					this.operandValue));
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

	private static final class LogicalEval implements Eval {

		private final LogicalResult ref;

		LogicalEval(LogicalResult ref) {
			this.ref = ref;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.returnValue(this.ref.logical.write(
					dirs.valDirs(),
					host,
					this.ref.operand().op(host),
					null));

		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

}
