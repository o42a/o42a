/*
    Parser
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.parser.Grammar.*;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.*;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class StatementParser implements Parser<StatementNode> {

	private final Grammar grammar;

	public StatementParser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public StatementNode parse(ParserContext context) {
		switch (context.next()) {
		case '=':
			return context.parse(this.grammar.selfAssignment());
		case '@':

			final DeclarableAdapterNode declarableAdapter =
				context.parse(declarableAdapter());

			if (declarableAdapter == null) {
				return null;
			}

			return context.parse(
					new DeclaratorParser(this.grammar, declarableAdapter));
		case '{':
			return context.parse(braces());
		case '<':
			return context.parse(this.grammar.clauseDeclarator());
		}

		final ExpressionNode expression =
			context.parse(this.grammar.expression());

		if (expression == null) {
			return null;
		}
		if (this.grammar == IMPERATIVE && context.next() == '=') {

			final AssignmentNode assignment =
				context.parse(assignment(expression));

			if (assignment != null) {
				return assignment;
			}
		}
		if (expression instanceof DeclarableNode) {

			final DeclarableNode declarable = (DeclarableNode) expression;

			final NamedBlockNode namedBlock =
				parseNamedBlock(context, declarable);

			if (namedBlock != null) {
				return namedBlock;
			}

			final DeclaratorNode declarator =
				context.parse(this.grammar.declarator(declarable));

			if (declarator != null) {
				return declarator;
			}
		}

		return expression;
	}

	private NamedBlockNode parseNamedBlock(
			ParserContext context,
			DeclarableNode declarable) {
		if (!(declarable instanceof MemberRefNode)) {
			return null;
		}

		final MemberRefNode ref = (MemberRefNode) declarable;

		if (ref.getOwner() != null || ref.getDeclaredIn() != null) {
			return null;
		}

		final NameNode name = ref.getName();

		if (name == null) {
			return null;
		}

		final NamedBlockNode namedBlock = context.parse(namedBlock(name));

		if (namedBlock != null) {
			name.addComments(ref.getComments());
		}

		return namedBlock;
	}

}
