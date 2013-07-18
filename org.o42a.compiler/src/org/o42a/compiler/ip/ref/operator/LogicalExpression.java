/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.codegen.code.Block;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.Distributor;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ValueFieldDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueType;


public class LogicalExpression extends ObjectConstructor {

	private final UnaryNode node;
	private final Ref operand;

	public LogicalExpression(
			Interpreter ip,
			CompilerContext context,
			UnaryNode node,
			AccessDistributor distributor,
			boolean stateful) {
		super(new Location(context, node), distributor, stateful);
		this.node = node;
		this.operand = this.node.getOperand().accept(
				ip.expressionVisitor(),
				distributor);
	}

	private LogicalExpression(LogicalExpression prototype, boolean stateful) {
		super(prototype, prototype.distribute(), stateful);
		this.node = prototype.node;
		this.operand = prototype.operand;
	}

	private LogicalExpression(
			LogicalExpression prototype,
			Distributor distributor,
			Ref operand) {
		super(prototype, distributor, prototype.isStateful());
		this.node = prototype.node;
		this.operand = operand;
	}

	public final UnaryNode getNode() {
		return this.node;
	}

	public final Ref operand() {
		return this.operand;
	}

	@Override
	public TypeRef ancestor(LocationInfo location, Ref ref) {
		return ValueType.VOID.typeRef(location, getScope());
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ancestor(ref, ref);
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return new ValueFieldDefinition(ref, null);
	}

	@Override
	public LogicalExpression reproduce(PathReproducer reproducer) {

		final Ref operand = this.operand.reproduce(reproducer.getReproducer());

		if (operand == null) {
			return null;
		}

		return new LogicalExpression(this, reproducer.distribute(), operand);
	}

	@Override
	public String toString() {
		if (this.operand != null) {
			return this.node.getOperator().getSign() + this.operand;
		}
		return super.toString();
	}

	@Override
	protected LogicalExpression createStateful() {
		return new LogicalExpression(this, true);
	}

	@Override
	protected Obj createObject() {
		return new LogicalResult(this);
	}

	ValOp write(ValDirs dirs, HostOp host, RefOp op, InlineValue inlineOp) {

		final CodeBuilder builder = dirs.getBuilder();
		final Block code = dirs.code();
		final UnaryOperator operator = this.node.getOperator();

		if (operator == UnaryOperator.NOT) {

			final Block operandFalse = dirs.addBlock("operand_false");

			writeCond(
					dirs.dirs().setFalseDir(operandFalse.head()),
					host,
					op,
					inlineOp);
			if (code.exists()) {
				code.go(dirs.falseDir());
			}
			if (operandFalse.exists()) {
				operandFalse.go(code.tail());
			}
		} else if (operator == UnaryOperator.IS_TRUE) {
			writeCond(dirs.dirs(), host, op, inlineOp);
		} else {
			throw new IllegalStateException(
					"Unsupported logical operator: "
					+ this.node.getOperator().getSign());
		}

		return builder.voidVal(code);
	}

	private final void writeCond(
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
