/*
    Abstract Syntax Tree
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.ast.type;

import org.o42a.ast.Node;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.AbstractRefVisitor;
import org.o42a.ast.ref.RefNode;


public abstract class AbstractTypeVisitor<R, P>
		extends AbstractRefVisitor<R, P>
		implements TypeNodeVisitor<R, P> {

	@Override
	public R visitAscendants(AscendantsNode ascendants, P p) {
		return visitType(ascendants, p);
	}

	@Override
	public R visitTypeParameters(TypeParametersNode parameters, P p) {
		return visitType(parameters, p);
	}

	@Override
	public R visitTypeArguments(TypeArgumentsNode arguments, P p) {
		return visitType(arguments, p);
	}

	@Override
	public R visitMacroExpression(MacroExpressionNode expression, P p) {
		return visitType(expression, p);
	}

	@Override
	protected R visitRef(RefNode ref, P p) {
		return visitType(ref, p);
	}

	@Override
	public R visitParentheses(ParenthesesNode parentheses, P p) {
		return visitType(parentheses, p);
	}

	protected abstract R visitType(Node type, P p);

}
