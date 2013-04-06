/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref;

import static org.o42a.compiler.ip.Interpreter.unwrap;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;


public final class RefBuildVisitor
		extends AbstractExpressionVisitor<RefBuilder, AccessDistributor> {

	private final ExpressionNodeVisitor<Ref, AccessDistributor> visitor;

	public RefBuildVisitor(
			ExpressionNodeVisitor<Ref, AccessDistributor> visitor) {
		this.visitor = visitor;
	}

	@Override
	public RefBuilder visitParentheses(
			ParenthesesNode parentheses,
			AccessDistributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	protected RefBuilder visitRef(RefNode ref, AccessDistributor p) {
		return new RefAccess<ExpressionNode>(ref, p) {
			@Override
			public Ref accessRef(AccessDistributor distributor) {
				return getNode().accept(
						RefBuildVisitor.this.visitor,
						distributor);
			}
		};
	}

	@Override
	protected RefBuilder visitExpression(
			ExpressionNode expression,
			AccessDistributor p) {
		return expression.accept(this.visitor, p);
	}

}
