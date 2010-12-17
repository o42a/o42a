/*
    Compiler
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
package org.o42a.compiler.ip.operator;

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.compiler.ip.operator.CompareResult.EqualsCompareResult;
import org.o42a.compiler.ip.operator.CompareResult.GreaterOrEqualsResult;
import org.o42a.compiler.ip.operator.CompareResult.GreaterResult;
import org.o42a.compiler.ip.operator.CompareResult.LessOrEqualsResult;
import org.o42a.compiler.ip.operator.CompareResult.LessResult;
import org.o42a.compiler.ip.operator.CompareResult.NotEqualsCompareResult;
import org.o42a.compiler.ip.operator.EqualsResult.NotEqualsResult;
import org.o42a.core.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.NewObjectEx;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;


public final class BinaryOp extends NewObjectEx {

	private BinaryOperatorLookup operator;

	public BinaryOp(
			CompilerContext context,
			BinaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
	}

	private BinaryOp(LocationSpec location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public BinaryNode getNode() {
		return (BinaryNode) super.getNode();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new BinaryOp(this, reproducer.distribute());
	}

	@Override
	protected Obj createObject() {

		final BinaryOperatorLookup operator = operator();

		if (operator.getResolution().isFalse()) {
			// operator not supported
			return null;
		}

		switch (getNode().getOperator()) {
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
			return operatorCall(
					operator,
					new RightOperand(
							getContext(),
							getNode().getRightOperand()));
		case EQUAL:
			if (!operator.isSecond()) {
				return new EqualsResult(this, distribute(), operator);
			}
			return new EqualsCompareResult(this, distribute(), operator);
		case NOT_EQUAL:
			if (!operator.isSecond()) {
				return new NotEqualsResult(this, distribute(), operator);
			}
			return new NotEqualsCompareResult(this, distribute(), operator);
		case LESS:
			return new LessResult(this, distribute(), operator);
		case LESS_OR_EQUAL:
			return new LessOrEqualsResult(this, distribute(), operator);
		case GREATER:
			return new GreaterResult(this, distribute(), operator);
		case GREATER_OR_EQUAL:
			return new GreaterOrEqualsResult(this, distribute(), operator);
		}

		throw new IllegalArgumentException(
				"Unsupported binary operator: "
				+ getNode().getOperator().getSign());
	}

	private BinaryOperatorLookup operator() {
		if (this.operator == null) {

			final Ref leftOperand = getNode().getLeftOperand().accept(
					EXPRESSION_VISITOR,
					distribute());

			this.operator = new BinaryOperatorLookup(leftOperand, getNode());
		}

		return this.operator;
	}

	private Obj operatorCall(Ref operator, BlockBuilder rightOperand) {
		return new OperatorCall(
				new Location(getContext(), getNode().getSign()),
				distribute(),
				operator.toTypeRef(),
				rightOperand);
	}

	private static final class OperatorCall extends DefinedObject {

		private final TypeRef operator;
		private final BlockBuilder rightOperand;

		OperatorCall(
				LocationSpec location,
				Distributor enclosing,
				TypeRef operator,
				BlockBuilder rightOperand) {
			super(location, enclosing);
			this.operator = operator;
			this.rightOperand = rightOperand;
		}

		@Override
		protected Ascendants buildAscendants() {
			return new Ascendants(this).setAncestor(this.operator);
		}

		@Override
		protected void buildDefinition(DeclarativeBlock definition) {
			this.rightOperand.buildBlock(definition);
		}

	}

}
