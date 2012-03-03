/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.value.Value.voidValue;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.codegen.code.Block;
import org.o42a.common.object.BuiltinObject;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public class LogicalExpression extends ObjectConstructor {

	private final UnaryNode node;
	private Ref operand;

	public LogicalExpression(
			Interpreter ip,
			CompilerContext context,
			UnaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
		this.operand = this.node.getOperand().accept(
				ip.derefExVisitor(),
				distribute());
	}

	private LogicalExpression(
			LogicalExpression prototype,
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
	public LogicalExpression reproduce(PathReproducer reproducer) {
		return new LogicalExpression(this, reproducer.getReproducer());
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
		return new LogicalResult(this);
	}

	private ValOp write(
			ValDirs dirs,
			HostOp host,
			RefOp op,
			InlineValue inlineOp) {

		final CodeBuilder builder = dirs.getBuilder();
		final Block code = dirs.code();
		final Block operandFalse = dirs.addBlock("operand_false");
		final Block operandUnknown = dirs.addBlock("operand_unknown");

		switch (this.node.getOperator()) {
		case NOT:
			writeLogicalValue(
					builder.falseWhenUnknown(code, operandFalse.head()),
					host,
					op,
					inlineOp);
			code.go(dirs.falseDir());
			if (operandFalse.exists()) {
				operandFalse.go(code.tail());
			}
			break;
		case IS_TRUE:
			writeLogicalValue(
					builder.falseWhenUnknown(code, operandFalse.head()),
					host,
					op,
					inlineOp);
			if (operandFalse.exists()) {
				operandFalse.go(dirs.falseDir());
			}
			break;
		case KNOWN:
			writeLogicalValue(
					builder.splitWhenUnknown(
							code,
							operandFalse.head(),
							operandUnknown.head()),
					host,
					op,
					inlineOp);
			if (operandFalse.exists()) {
				operandFalse.go(code.tail());
			}
			if (operandUnknown.exists()) {
				operandUnknown.go(dirs.falseDir());
			}
			break;
		case UNKNOWN:
			writeLogicalValue(
					builder.splitWhenUnknown(
							code,
							operandFalse.head(),
							operandUnknown.head()),
					host,
					op,
					inlineOp);
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
					+ this.node.getOperator().getSign());
		}

		return voidValue().op(builder, code);
	}

	private final void writeLogicalValue(
			CodeDirs dirs,
			HostOp host,
			RefOp op,
			InlineValue inlineOp) {
		if (inlineOp != null) {
			inlineOp.writeCond(dirs, host);
		} else {
			op.writeLogicalValue(dirs);
		}
	}

	private static final class LogicalResult extends BuiltinObject {

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

			switch (this.ref.node.getOperator()) {
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
						+ this.ref.node.getOperator().getSign());
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

		final Ref operand() {
			if (this.operand != null) {
				return this.operand;
			}
			return this.operand = this.ref.operand.rescope(getScope());
		}

	}

	private static final class Inline extends InlineValue {

		private final LogicalExpression ref;
		private final InlineValue operandValue;

		Inline(
				ValueStruct<?, ?> valueStruct,
				LogicalExpression ref,
				InlineValue operandValue) {
			super(valueStruct);
			this.ref = ref;
			this.operandValue = operandValue;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return this.ref.write(dirs, host, null, this.operandValue);
		}

		@Override
		public void cancel() {
			this.operandValue.cancel();
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
