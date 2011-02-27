/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.common.adapter.UnaryOperatorInfo;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.Location;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Call;
import org.o42a.core.ref.common.Wrap;


public class UnaryOperatorRef extends Wrap {

	private final UnaryNode node;
	private final Ref operand;

	public UnaryOperatorRef(
			CompilerContext context,
			UnaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
		this.operand =
			node.getOperand().accept(EXPRESSION_VISITOR, distribute());
	}

	@Override
	public String toString() {
		if (this.operand == null) {
			return super.toString();
		}
		return this.node.getOperator().getSign() + this.operand;
	}

	@Override
	protected Ref resolveWrapped() {

		final Resolution operandResolution = this.operand.getResolution();

		if (operandResolution.isError()) {
			return errorRef(operandResolution, distribute());
		}

		final Obj operand = operandResolution.materialize();
		final Location operatorLocation =
			new Location(getContext(), this.node.getSign());
		final UnaryOperatorInfo info =
			UnaryOperatorInfo.bySign(this.node.getOperator().getSign());
		final AdapterId adapterId =
			info.getPath().toAdapterId(operatorLocation, distribute());
		final Member adapter = operand.member(adapterId);

		if (adapter == null) {
			getLogger().error(
					"unsupported_unary_operator",
					operatorLocation,
					"Unary operator '%s' is not supported, "
					+ "because operand doesn't have an '%s' adapter",
					info,
					adapterId);
			return errorRef(operatorLocation);
		}

		final Ref adapterRef = adapter.getKey().toPath().target(
				operatorLocation,
				distribute(),
				this.operand.materialize());

		return new Call(
				this,
				distribute(),
				new AscendantsDefinition(
						adapterRef,
						adapterRef.distribute(),
						adapterRef.toTypeRef()),
				emptyBlock(this));
	}

}
