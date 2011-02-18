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
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.Location;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.DefinitionValue;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Wrap;


public class UnaryOp extends Wrap {

	private final UnaryNode node;

	public UnaryOp(
			CompilerContext context,
			UnaryNode node,
			Distributor distributor) {
		super(new Location(context, node), distributor);
		this.node = node;
	}

	public UnaryNode getNode() {
		return this.node;
	}

	@Override
	public String toString() {

		final Ref wrapped = getWrapped();

		if (wrapped != null) {
			return wrapped.toString();
		}
		if (this.node == null) {
			return super.toString();
		}

		return "Unary " + this.node.getSign();
	}

	@Override
	protected Ref resolveWrapped() {

		final UnaryOperatorType type =
			UnaryOperatorType.byOperator(this.node.getOperator());
		final Ref operandRef =
			getNode().getOperand().accept(EXPRESSION_VISITOR, distribute());
		final Resolution operandResolution = operandRef.getResolution();
		final Obj operand = operandResolution.materialize();
		final Location operatorLocation =
			new Location(getContext(), this.node.getSign());
		final AdapterId adapterId =
			type.getPath().toAdapterId(operatorLocation, distribute());
		final Member adapter = operand.member(adapterId);

		if (adapter == null) {
			getLogger().error(
					"unsupported_unary_operator",
					operatorLocation,
					"Unary operator '%s' is not supported, "
					+ "because operand doesn't have a '%s' adapter",
					type,
					adapterId);
			return null;
		}

		final Ref adapterRef = adapter.getKey().toPath().target(
				operatorLocation,
				distribute(),
				operandRef.materialize());

		return new DefinitionValue(
				this,
				distribute(),
				new AscendantsDefinition(
						adapterRef,
						adapterRef.distribute(),
						adapterRef.toTypeRef()),
				emptyBlock(this));
	}

}
