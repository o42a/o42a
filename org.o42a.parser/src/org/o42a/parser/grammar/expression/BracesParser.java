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

import static org.o42a.parser.Grammar.IMPERATIVE;

import org.o42a.ast.atom.BraceSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Parser;


public class BracesParser extends AbstractBlockParser<BracesNode, BraceSign> {

	public static final Parser<BracesNode> BRACES = new BracesParser();

	private BracesParser() {
		super(BraceSign.OPENING_BRACE, BraceSign.CLOSING_BRACE);
	}

	@Override
	protected Parser<SentenceNode[]> getContentParser() {
		return IMPERATIVE.content();
	}

	@Override
	protected BracesNode createBlock(
			SignNode<BraceSign> opening,
			SentenceNode[] content,
			SignNode<BraceSign> closing) {
		return new BracesNode(opening, content, closing);
	}

}
