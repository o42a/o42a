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
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeArgumentsNode;


public interface ExpressionNodeVisitor<R, P> extends RefNodeVisitor<R, P> {

	R visitNumber(NumberNode number, P p);

	R visitText(TextNode text, P p);

	R visitAscendants(AscendantsNode ascendants, P p);

	R visitTypeArguments(TypeArgumentsNode arguments, P p);

	R visitGroup(GroupNode group, P p);

	R visitUnary(UnaryNode expression, P p);

	R visitBinary(BinaryNode expression, P p);

	R visitBrackets(BracketsNode brackets, P p);

	R visitParentheses(ParenthesesNode parentheses, P p);

	R visitPhrase(PhraseNode phrase, P p);

}
