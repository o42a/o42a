/*
    Parser
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
package org.o42a.parser.grammar.expression;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.BracesNode.Brace;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;


public class BracesParser extends AbstractBlockParser<BracesNode, Brace> {

	public static final Parser<BracesNode> BRACES = new BracesParser();

	private BracesParser() {
		super(Brace.OPENING_BRACE, Brace.CLOSING_BRACE);
	}

	@Override
	protected Parser<SentenceNode[]> getContentParser() {
		return Grammar.IMPERATIVE.content();
	}

	@Override
	protected BracesNode createBlock(
			SignNode<Brace> opening,
			SentenceNode[] content,
			SignNode<Brace> closing) {
		return new BracesNode(opening, content, closing);
	}

}
