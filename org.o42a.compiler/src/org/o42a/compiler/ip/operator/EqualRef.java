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

import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.Location;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Wrap;


final class EqualRef extends Wrap {

	private final BinaryNode node;
	private final Ref leftOperand;
	private final Ref rightOperand;

	public EqualRef(
			CompilerContext context,
			BinaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
		this.leftOperand = node.getLeftOperand().accept(
				EXPRESSION_VISITOR,
				distributor);
		this.rightOperand = node.getRightOperand().accept(
				EXPRESSION_VISITOR,
				distributor);
	}

	@Override
	public String toString() {
		if (this.rightOperand == null) {
			return super.toString();
		}

		return this.leftOperand
		+ this.node.getOperator().getSign()
		+ this.rightOperand;
	}

	@Override
	protected Ref resolveWrapped() {

		final boolean equal = this.node.getOperator() == BinaryOperator.EQUAL;
		final Resolution leftResolution = this.leftOperand.getResolution();

		if (leftResolution.isError()) {
			return errorRef(leftResolution);
		}

		final Obj left = leftResolution.materialize();
		final Location operatorLocation =
			new Location(getContext(), this.node.getSign());
		final AdapterId adapterId1 =
			BinaryOperatorInfo.EQUAL.getPath().toAdapterId(
					operatorLocation,
					distribute());
		final Member adapter1 = left.member(adapterId1);

		if (adapter1 != null) {
			if (equal) {
				return new EqualityRef.Equal(
						this,
						distribute(),
						this.node,
						this.leftOperand,
						this.rightOperand);
			}

			return new EqualityRef.NotEqual(
					this,
					distribute(),
					this.node,
					this.leftOperand,
					this.rightOperand);
		}

		final AdapterId adapterId2 =
			BinaryOperatorInfo.COMPARE.getPath().toAdapterId(
					operatorLocation,
					distribute());
		final Member adapter2 = left.member(adapterId2);

		if (adapter2 != null) {
			if (equal) {
				return new ComparisonRef.EqualComparison(
						this,
						distribute(),
						this.node,
						this.leftOperand,
						this.rightOperand);
			}
			return new ComparisonRef.NotEqualComparison(
					this,
					distribute(),
					this.node,
					this.leftOperand,
					this.rightOperand);
		}

		getLogger().error(
				"unsupported_binary_operator",
				operatorLocation,
				"Binary operator '%s' is not supported, "
				+ "because left operand has neither '%s', nor '%s' adapter",
				this.node.getOperator().getSign(),
				adapterId1,
				adapterId2);

		return errorRef(operatorLocation);
	}

}
