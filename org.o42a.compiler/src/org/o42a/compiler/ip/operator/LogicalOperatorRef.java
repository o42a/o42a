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
import static org.o42a.core.ir.op.CodeDirs.exitWhenUnknown;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.core.*;
import org.o42a.core.artifact.common.Result;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Ref;
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

	private LogicalOperatorRef(LogicalOperatorRef sample, Reproducer reproducer) {
		super(sample, reproducer.distribute());
		this.node = sample.node;
		this.operand = sample.operand.reproduce(reproducer);
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

	private static final class Res extends Result {

		private final LogicalOperatorRef ref;

		Res(LogicalOperatorRef ref) {
			super(ref, ref.distributeIn(ref.getContainer()), ValueType.VOID);
			this.ref = ref;
		}

		@Override
		public String toString() {
			return this.ref != null ? this.ref.toString() : "LogicalOp";
		}

		@Override
		protected Value<?> calculateValue(Scope scope) {

			final Value<?> value =
				this.ref.operand.value(scope.getEnclosingScope());

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
		protected ObjectValueIR createValueIR(ObjectIR objectIR) {
			return new ValueIR(objectIR, this);
		}

	}

	private static final class ValueIR extends ProposedValueIR {

		private final Res res;

		ValueIR(ObjectIR objectIR, Res res) {
			super(objectIR);
			this.res = res;
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final Code failure = code.addBlock("failure");
			final Ref enclosingRef =
				this.res.getScope().getEnclosingScopePath()
				.target(this.res, this.res.distribute());

			final ValOp operandValue = code.allocate(VAL_TYPE);
			final CodeDirs dirs = exitWhenUnknown(code, failure.head());
			final HostOp operandHost = enclosingRef.op(host).target(dirs);

			final CodeBlk returnTrue = code.addBlock("return_true");
			final CodeBlk returnFalse = code.addBlock("return_false");

			this.res.ref.op(operandHost).writeValue(dirs, operandValue);

			switch (this.res.ref.node.getOperator()) {
			case NOT:
				operandValue.loadCondition(code).go(
						code,
						returnFalse.head(),
						returnTrue.head());
				break;
			case IS_TRUE:
				operandValue.loadCondition(code).go(
						code,
						returnTrue.head(),
						returnFalse.head());
				break;
			case KNOWN:
				operandValue.loadUnknown(code).go(
						code,
						returnFalse.head(),
						returnTrue.head());
				break;
			case UNKNOWN:
				operandValue.loadUnknown(code).go(
						code,
						returnTrue.head(),
						returnFalse.head());
				break;
			default:
				throw new IllegalStateException(
						"Unsupported logical operator: "
						+ this.res.ref.node.getOperator().getSign());
			}

			if (returnTrue.exists()) {
				result.storeVoid(returnTrue);
				returnTrue.go(code.tail());
			}
			if (returnFalse.exists()) {
				result.storeFalse(returnFalse);
				returnFalse.go(code.tail());
			}
			if (failure.exists()) {
				result.storeFalse(failure);
				failure.go(code.tail());
			}
		}

	}

}
