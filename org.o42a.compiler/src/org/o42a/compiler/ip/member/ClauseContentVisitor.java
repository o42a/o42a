/*
    Compiler
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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.addContent;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.member.ClauseExpressionVisitor.CLAUSE_EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.member.ClauseInterpreter.buildOverrider;

import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.statement.*;
import org.o42a.core.CompilerLogger;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.st.St;
import org.o42a.core.st.sentence.Group;
import org.o42a.core.st.sentence.Statements;


final class ClauseContentVisitor
		extends AbstractStatementVisitor<St, Statements<?>> {

	private final ClauseDeclaration declaration;
	private final ClauseDeclaratorNode node;

	public ClauseContentVisitor(
			ClauseDeclaration declaration,
			ClauseDeclaratorNode node) {
		this.declaration = declaration;
		this.node = node;
	}

	@Override
	public St visitParentheses(ParenthesesNode parentheses, Statements<?> p) {

		final Group group = p.group(
				location(this.declaration, parentheses),
				this.declaration.setKind(ClauseKind.GROUP));

		if (group == null) {
			return null;
		}

		reuseClauses(group.getBuilder());
		addContent(
				new ClauseStatementVisitor(p.getContext()),
				group.parentheses(),
				parentheses);

		return null;
	}

	@Override
	public St visitBraces(BracesNode braces, Statements<?> p) {

		final Group group = p.group(
				location(this.declaration, braces),
				this.declaration.setKind(ClauseKind.GROUP));

		if (group == null) {
			return null;
		}

		reuseClauses(group.getBuilder());
		addContent(
				new ClauseStatementVisitor(p.getContext()),
				group.braces(null),
				braces);

		return null;
	}

	@Override
	public St visitNamedBlock(NamedBlockNode block, Statements<?> p) {

		final BracesNode braces = block.getBlock();
		final Group group = p.group(
				location(this.declaration, block.getName()),
				this.declaration.setKind(ClauseKind.GROUP));

		if (group == null) {
			return null;
		}

		reuseClauses(group.getBuilder());
		addContent(
				new ClauseStatementVisitor(p.getContext()),
				group.braces(block.getName().getName()),
				braces);

		return null;
	}

	@Override
	public St visitDeclarator(DeclaratorNode declarator, Statements<?> p) {

		final ClauseBuilder builder =
			buildOverrider(this.declaration, declarator, p);

		if (builder == null) {
			return null;
		}

		return reuseClauses(builder).build();
	}

	@Override
	public St visitSelfAssignment(
			SelfAssignmentNode assignment,
			Statements<?> p) {

		final ExpressionNode value = assignment.getValue();

		if (value == null) {
			return null;
		}

		final ClauseBuilder builder = builder(p, this.declaration);

		if (builder == null) {
			return null;
		}

		return buildExpression(builder.assignment(), value);
	}

	@Override
	protected St visitExpression(ExpressionNode expression, Statements<?> p) {
		return expression(expression, p, this.declaration);
	}

	@Override
	protected St visitStatement(StatementNode statement, Statements<?> p) {
		getLogger().invalidClauseContent(statement);
		return null;
	}

	private final CompilerLogger getLogger() {
		return this.declaration.getContext().getLogger();
	}

	private final ClauseBuilder reuseClauses(ClauseBuilder builder) {
		return ClauseInterpreter.reuseClauses(builder, this.node);
	}

	private St expression(
			ExpressionNode expression,
			Statements<?> p,
			ClauseDeclaration declaration) {

		final ClauseBuilder builder = builder(p, declaration);

		if (builder == null) {
			return null;
		}

		return buildExpression(builder, expression);
	}

	private ClauseBuilder builder(
			Statements<?> p,
			ClauseDeclaration declaration) {

		final ClauseBuilder builder = p.clause(declaration);

		if (builder == null) {
			return null;
		}

		return reuseClauses(builder);
	}

	private St buildExpression(
			ClauseBuilder builder,
			ExpressionNode expression) {
		builder = expression.accept(CLAUSE_EXPRESSION_VISITOR, builder);
		if (builder == null) {
			return null;
		}
		return builder.build();
	}

}
