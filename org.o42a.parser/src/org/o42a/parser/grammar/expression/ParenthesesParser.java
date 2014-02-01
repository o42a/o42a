/*
    Parser
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
package org.o42a.parser.grammar.expression;

import org.o42a.ast.atom.ParenthesisSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;


public class ParenthesesParser
		extends AbstractBlockParser<ParenthesesNode, ParenthesisSign> {

	private final Grammar grammar;

	public ParenthesesParser(Grammar grammar) {
		super(ParenthesisSign.OPENING_PARENTHESIS, ParenthesisSign.CLOSING_PARENTHESIS);
		this.grammar = grammar;
	}

	@Override
	protected Parser<SentenceNode[]> getContentParser() {
		return this.grammar.content();
	}

	@Override
	protected ParenthesesNode createBlock(
			SignNode<ParenthesisSign> opening,
			SentenceNode[] content,
			SignNode<ParenthesisSign> closing) {
		return new ParenthesesNode(opening, content, closing);
	}

}
