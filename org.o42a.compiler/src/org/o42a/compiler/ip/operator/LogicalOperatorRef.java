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

import static org.o42a.core.artifact.object.ValuePart.PROPOSITION;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.codegen.code.Code;
import org.o42a.common.object.BuiltinObject;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.User;


public class LogicalOperatorRef extends ObjectConstructor {

	private final UnaryNode node;
	private Ref operand;

	public LogicalOperatorRef(
			Interpreter ip,
			CompilerContext context,
			UnaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
		this.operand =
			this.node.getOperand().accept(ip.expressionVisitor(), distribute());
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
		public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

			final CodeBuilder builder = dirs.getBuilder();
			final RefOp op = operand().op(host);
			final Code code = dirs.code();
			final Code operandFalse = dirs.addBlock("operand_false");
			final Code operandUnknown = dirs.addBlock("operand_unknown");

			switch (this.ref.node.getOperator()) {
			case NOT:
				op.writeLogicalValue(builder.falseWhenUnknown(
						code,
						operandFalse.head()));
				code.go(dirs.falseDir());
				if (operandFalse.exists()) {
					operandFalse.go(code.tail());
				}
				break;
			case IS_TRUE:
				op.writeLogicalValue(builder.falseWhenUnknown(
						code,
						operandFalse.head()));
				if (operandFalse.exists()) {
					operandFalse.go(dirs.falseDir());
				}
				break;
			case KNOWN:
				op.writeLogicalValue(builder.splitWhenUnknown(
						code,
						operandFalse.head(),
						operandUnknown.head()));
				if (operandFalse.exists()) {
					operandFalse.go(code.tail());
				}
				if (operandUnknown.exists()) {
					operandUnknown.go(dirs.falseDir());
				}
				break;
			case UNKNOWN:
				op.writeLogicalValue(builder.splitWhenUnknown(
						code,
						operandFalse.head(),
						operandUnknown.head()));
				code.go(dirs.falseDir());
				if (operandFalse.exists()) {
					operandFalse.go(dirs.falseDir());
				}
				if (operandUnknown.exists()) {
					operandUnknown.go(code.tail());
				}
				break;
			default:
				throw new IllegalStateException(
						"Unsupported logical operator: "
						+ this.ref.node.getOperator().getSign());
			}

			return voidValue().op(builder, code);
		}

		@Override
		public void resolveBuiltin(Obj object) {

			final User user = object.value().partUser(PROPOSITION);
			final Resolver resolver = object.getScope().newResolver(user);

			operand().resolveValues(resolver);
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
