/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.ir.op.CodeDirs.splitWhenUnknown;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.codegen.code.Code;
import org.o42a.common.object.BuiltinObject;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class LogicalOperatorRef extends ObjectConstructor {

	private final UnaryNode node;
	private Ref operand;

	public LogicalOperatorRef(
			CompilerContext context,
			UnaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
		this.operand =
			this.node.getOperand().accept(EXPRESSION_VISITOR, distribute());
	}

	private LogicalOperatorRef(
			LogicalOperatorRef prototype,
			Reproducer reproducer) {
		super(prototype, reproducer.distribute());
		this.node = prototype.node;
		this.operand = prototype.operand.reproduce(reproducer);
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		return new LogicalOperatorRef(this, reproducer);
	}

	@Override
	public String toString() {
		if (this.node != null) {
			return this.node.getOperator().getSign() + this.operand;
		}
		return super.toString();
	}

	@Override
	protected Obj createObject() {
		return new Res(this);
	}

	private static final class Res extends BuiltinObject {

		private final LogicalOperatorRef ref;
		private Ref operand;

		Res(LogicalOperatorRef ref) {
			super(ref, ref.distributeIn(ref.getContainer()), ValueType.VOID);
			this.ref = ref;
		}

		@Override
		public Value<?> calculateBuiltin(Resolver resolver) {

			final Value<?> value = operand().value(resolver);

			if (!value.isDefinite()) {
				return ValueType.VOID.runtimeValue();
			}

			switch (this.ref.node.getOperator()) {
			case NOT:
				if (value.isFalse()) {
					return Value.voidValue();
				}
				return Value.falseValue();
			case IS_TRUE:
				if (value.isFalse()) {
					return Value.falseValue();
				}
				return Value.voidValue();
			case KNOWN:
				if (value.isUnknown()) {
					return Value.falseValue();
				}
				return Value.voidValue();
			case UNKNOWN:
				if (value.isUnknown()) {
					return Value.voidValue();
				}
				return Value.falseValue();
			default:
				throw new IllegalStateException(
						"Unsupported logical operator: "
						+ this.ref.node.getOperator().getSign());
			}
		}

		@Override
		public void writeBuiltin(Code code, ValOp result, HostOp host) {

			final RefOp op = operand().op(host);
			final Code operandFalse = code.addBlock("operand_false");
			final Code operandUnknown = code.addBlock("operand_unknown");

			switch (this.ref.node.getOperator()) {
			case NOT:
				op.writeLogicalValue(falseWhenUnknown(
						code,
						operandFalse.head()));
				result.storeFalse(code);
				if (operandFalse.exists()) {
					result.storeVoid(operandFalse);
					operandFalse.go(code.tail());
				}
				break;
			case IS_TRUE:
				op.writeLogicalValue(falseWhenUnknown(
						code,
						operandFalse.head()));
				result.storeVoid(code);
				if (operandFalse.exists()) {
					result.storeFalse(operandFalse);
					operandFalse.go(code.tail());
				}
				break;
			case KNOWN:
				op.writeLogicalValue(splitWhenUnknown(
						code,
						null,
						operandUnknown.head()));
				result.storeVoid(code);
				if (operandUnknown.exists()) {
					result.storeFalse(operandUnknown);
					operandUnknown.go(code.tail());
				}
				break;
			case UNKNOWN:
				op.writeLogicalValue(splitWhenUnknown(
						code,
						null,
						operandUnknown.head()));
				result.storeFalse(code);
				if (operandUnknown.exists()) {
					result.storeVoid(operandUnknown);
					operandUnknown.go(code.tail());
				}
				break;
			default:
				throw new IllegalStateException(
						"Unsupported logical operator: "
						+ this.ref.node.getOperator().getSign());
			}
		}

		@Override
		public void resolveBuiltin(Obj object) {
			operand().resolveValues(object.getScope().newResolver());
		}

		@Override
		public String toString() {
			return this.ref != null ? this.ref.toString() : "LogicalOp";
		}

		final Ref operand() {
			if (this.operand != null) {
				return this.operand;
			}
			return this.operand = this.ref.operand.rescope(getScope());
		}

	}

}
