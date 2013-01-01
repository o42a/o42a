/*
    Parser
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.parser.grammar.expression;

import static org.o42a.parser.Grammar.simpleExpression;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.MacroExpansionNode;
import org.o42a.ast.expression.UnaryOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class MacroExpansionParser implements Parser<MacroExpansionNode> {

	public static final MacroExpansionParser MACRO_EXPANSION =
			new MacroExpansionParser();

	private MacroExpansionParser() {
	}

	@Override
	public MacroExpansionNode parse(ParserContext context) {
		if (context.next() != '#') {
			return null;
		}

		final SourcePosition start = context.current().fix();

		context.skip();

		final SignNode<UnaryOperator> sign = new SignNode<UnaryOperator>(
				start,
				context.current().fix(),
				UnaryOperator.MACRO_EXPANSION);

		if (context.next() == '#') {
			// The macro expansion expression requires exactly one hash.
			return null;
		}
		context.acceptComments(false, sign);

		final ExpressionNode operand = context.parse(simpleExpression());

		if (operand == null) {
			context.getLogger().missingOperand(context.current(), "#");
		}

		return new MacroExpansionNode(sign, operand);
	}

}
