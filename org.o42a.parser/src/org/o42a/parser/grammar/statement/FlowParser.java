/*
    Parser
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.parser.grammar.statement;

import static org.o42a.parser.Grammar.braces;
import static org.o42a.parser.grammar.statement.FlowOperatorParser.FLOW_OPERATOR;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.statement.FlowNode;
import org.o42a.ast.statement.FlowOperator;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class FlowParser implements Parser<FlowNode> {

	private final NameNode name;

	public FlowParser(NameNode name) {
		this.name = name;
	}

	@Override
	public FlowNode parse(ParserContext context) {

		final SignNode<FlowOperator> operator = context.push(FLOW_OPERATOR);

		if (operator == null) {
			return null;
		}

		final BracesNode block = context.parse(braces());

		if (block == null) {
			return null;
		}

		return new FlowNode(this.name, operator, block);
	}

}
