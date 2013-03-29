/*
    Parser
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
package org.o42a.parser.grammar.statement;

import static org.o42a.parser.Grammar.*;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.MacroExpansionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.statement.*;
import org.o42a.parser.Grammar;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class StatementParser implements Parser<StatementNode> {

	private final Grammar grammar;
	private final boolean local;

	public StatementParser(Grammar grammar, boolean local) {
		this.grammar = grammar;
		this.local = local;
	}

	@Override
	public StatementNode parse(ParserContext context) {

		SourcePosition firstUnexpected = null;

		for (;;) {

			final SourcePosition start = context.current().fix();
			final StatementNode statement = parseNonExpression(context);

			if (statement != null) {
				context.logUnexpected(firstUnexpected, start);
				context.acceptUnexpected();
				return statement;
			}
			if (context.isEOF()) {
				if (firstUnexpected != null) {
					context.logUnexpected(firstUnexpected, start);
				} else {
					context.getLogger().eof(start);
				}
				return null;
			}

			final ExpressionNode expression = context.parse(expression());

			if (expression != null) {
				context.logUnexpected(firstUnexpected, start);

				final StatementNode result =
						startWithExpression(context, expression);

				context.acceptUnexpected();

				return result;
			}
			// Unexpected input before the statement.
			// Possibly multiple lines.
			if (!context.unexpected()) {
				context.logUnexpected(firstUnexpected, start);
				return null;
			}
			if (firstUnexpected == null) {
				firstUnexpected = start;
			}
			context.acceptAll();
			if (context.acceptComments(true) != null) {
				context.logUnexpected(firstUnexpected, start);
				firstUnexpected = null;
			}
		}
	}

	private StatementNode parseNonExpression(ParserContext context) {
		if (this.local) {
			return parseLocalStatement(context);
		}

		switch (context.next()) {
		case '=':
			return context.parse(selfAssignment());
		case '@':

			final DeclarableAdapterNode declarableAdapter =
					context.parse(declarableAdapter());

			if (declarableAdapter == null) {
				return null;
			}

			return context.parse(declarator(declarableAdapter));
		case '{':
			return context.parse(braces());
		case '(':
			return parseParentheses(context);
		case '<':
			return context.parse(this.grammar.clauseDeclarator());
		case '*':
			if (this.grammar.isDeclarative()) {
				return context.parse(inclusion());
			}
		}

		return null;
	}

	private StatementNode parseLocalStatement(ParserContext context) {
		switch (context.next()) {
		case '{':
			return context.parse(braces());
		case '(':
			return parseParentheses(context);
		}
		return null;
	}

	private StatementNode parseParentheses(ParserContext context) {

		final ParenthesesNode parentheses =
				context.parse(this.grammar.parentheses());

		if (parentheses == null) {
			return null;
		}

		final ExpressionNode expression =
				context.parse(expression(parentheses));
		final ExpressionNode result;

		if (expression != null) {
			result = expression;
		} else {
			result = parentheses;
		}

		return startWithExpression(context, result);
	}

	private StatementNode startWithExpression(
			ParserContext context,
			ExpressionNode expression) {

		final LocalNode local = context.parse(local(expression));
		final StatementNode assignment =
				parseAssignment(context, local, expression);

		if (assignment != null) {
			return assignment;
		}

		final LocalScopeNode localScope =
				parseLocalScope(context, local, expression);

		if (localScope != null) {
			return localScope;
		}

		return startWithDeclarable(context, expression);
	}

	private AssignmentNode parseAssignment(
			ParserContext context,
			LocalNode local,
			ExpressionNode expression) {
		if (local == null && !assignmentsAllowed()) {
			return null;
		}
		if (context.pendingOrNext() != '=') {
			return null;
		}
		return context.parse(assignment(local != null ? local : expression));
	}

	private LocalScopeNode parseLocalScope(
			ParserContext context,
			LocalNode local,
			ExpressionNode expression) {
		if (local != null) {
			return context.parse(this.grammar.localScope(local));
		}
		return context.parse(this.grammar.localScope(expression));
	}

	private boolean assignmentsAllowed() {
		return this.local || this.grammar.isImperative();
	}

	private StatementNode startWithDeclarable(
			ParserContext context,
			ExpressionNode expression) {

		final DeclarableNode declarable = expression.toDeclarable();

		if (declarable == null) {
			return expression;
		}

		final NamedBlockNode namedBlock =
				parseNamedBlock(context, declarable);

		if (namedBlock != null) {
			return namedBlock;
		}

		if (this.local) {
			// Declarations not allowed inside local scope.
			return expression;
		}

		final MacroExpansionNode expansion = declarable.toMacroExpansion();

		if (expansion != null) {
			if (this.grammar.isImperative()) {
				return expression;
			}

			final RefNode expandedRef = expansion.getOperand().toRef();

			if (expandedRef == null || expandedRef.toMemberRef() == null) {
				return expression;
			}
		}

		final DeclaratorNode declarator = context.parse(declarator(declarable));

		if (declarator != null) {
			return declarator;
		}

		return expression;
	}

	private NamedBlockNode parseNamedBlock(
			ParserContext context,
			DeclarableNode declarable) {

		final MemberRefNode memberRef = declarable.toMemberRef();

		if (memberRef == null) {
			return null;
		}
		if (memberRef.getOwner() != null || memberRef.getDeclaredIn() != null) {
			return null;
		}

		final NameNode name = memberRef.getName();

		if (name == null) {
			return null;
		}

		final NamedBlockNode namedBlock = context.parse(namedBlock(name));

		if (namedBlock != null) {
			name.addComments(memberRef.getComments());
		}

		return namedBlock;
	}

}
