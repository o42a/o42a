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
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;


public class LogicalExpression extends ObjectConstructor {

	private final UnaryNode node;
	private final Ref operand;

	public LogicalExpression(
			Interpreter ip,
			CompilerContext context,
			UnaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
		this.operand = this.node.getOperand().accept(
				ip.targetExVisitor(),
				distribute());
	}

	private LogicalExpression(
			LogicalExpression prototype,
			Reproducer reproducer) {
		super(prototype, reproducer.distribute());
		this.node = prototype.node;
		this.operand = prototype.operand.reproduce(reproducer);
	}

	public final UnaryNode getNode() {
		return this.node;
	}

	public final Ref operand() {
		return this.operand;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	@Override
	public FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return new ValueFieldDefinition(path, distributor);
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

	ValOp write(
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
			op.writeCond(dirs);
		}
	}

}
