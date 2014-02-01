/*
    Abstract Syntax Tree
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.ast.expression;

import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.ref.AbstractRefVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeArgumentsNode;


public abstract class AbstractExpressionVisitor<R, P>
		extends AbstractRefVisitor<R, P>
		implements ExpressionNodeVisitor<R, P> {

	@Override
	public R visitNumber(NumberNode number, P p) {
		return visitExpression(number, p);
	}

	@Override
	public R visitText(TextNode text, P p) {
		return visitExpression(text, p);
	}

	@Override
	public R visitAscendants(AscendantsNode ascendants, P p) {
		return visitExpression(ascendants, p);
	}

	@Override
	public R visitTypeArguments(TypeArgumentsNode arguments, P p) {
		return visitExpression(arguments, p);
	}

	@Override
	public R visitGroup(GroupNode group, P p) {
		return visitExpression(group, p);
	}

	@Override
	public R visitUnary(UnaryNode expression, P p) {
		return visitExpression(expression, p);
	}

	@Override
	public R visitBinary(BinaryNode expression, P p) {
		return visitExpression(expression, p);
	}

	@Override
	public R visitBrackets(BracketsNode brackets, P p) {
		return visitExpression(brackets, p);
	}

	@Override
	public R visitParentheses(ParenthesesNode parentheses, P p) {
		return visitExpression(parentheses, p);
	}

	@Override
	public R visitPhrase(PhraseNode phrase, P p) {
		return visitExpression(phrase, p);
	}

	@Override
	protected R visitRef(RefNode ref, P p) {
		return visitExpression(ref, p);
	}

	protected abstract R visitExpression(ExpressionNode expression, P p);

}
