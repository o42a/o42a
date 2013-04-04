/*
    Compiler
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
package org.o42a.compiler.ip.clause;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.clause.ClauseExpressionVisitor.CLAUSE_EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.clause.ClauseInterpreter.buildOverrider;
import static org.o42a.compiler.ip.clause.ClauseInterpreter.invalidClauseContent;
import static org.o42a.compiler.ip.st.StInterpreter.addContent;

import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.statement.*;
import org.o42a.compiler.ip.st.StatementsAccess;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.sentence.Group;
import org.o42a.core.st.sentence.ImperativeBlock;


final class ClauseContentVisitor
		extends AbstractStatementVisitor<Void, StatementsAccess> {

	private final ClauseDeclaration declaration;
	private final ClauseDeclaratorNode node;

	public ClauseContentVisitor(
			ClauseDeclaration declaration,
			ClauseDeclaratorNode node) {
		this.declaration = declaration;
		this.node = node;
	}

	@Override
	public Void visitParentheses(
			ParenthesesNode parentheses,
			StatementsAccess p) {

		final Group group = p.get().group(
				location(this.declaration, parentheses),
				this.declaration.setKind(ClauseKind.GROUP));

		if (group == null) {
			return null;
		}

		final ClauseAccess builder =
				new ClauseAccess(p.getRules(), group.getBuilder());

		declare(builder);
		addContent(
				builder.getRules().contentRules(),
				new ClauseStatementVisitor(p.getContext()),
				group.parentheses(),
				parentheses);

		return null;
	}

	@Override
	public Void visitBraces(BracesNode braces, StatementsAccess p) {

		final Group group = p.get().group(
				location(this.declaration, braces),
				this.declaration.setKind(ClauseKind.GROUP));

		if (group == null) {
			return null;
		}

		final ClauseAccess builder =
				new ClauseAccess(p.getRules(), group.getBuilder());

		declare(builder);

		final ImperativeBlock bracesBlock = group.braces(null);

		if (bracesBlock != null) {
			addContent(
					builder.getRules().contentRules(),
					new ClauseStatementVisitor(p.getContext()),
					bracesBlock,
					braces);
		}

		return null;
	}

	@Override
	public Void visitNamedBlock(NamedBlockNode block, StatementsAccess p) {

		final BracesNode braces = block.getBlock();
		final Group group = p.get().group(
				location(this.declaration, block.getName()),
				this.declaration.setKind(ClauseKind.GROUP));

		if (group == null) {
			return null;
		}

		final ClauseAccess builder =
				new ClauseAccess(p.getRules(), group.getBuilder());

		declare(builder);

		final ImperativeBlock bracesBlock =
				group.braces(block.getName().getName());

		if (bracesBlock != null) {
			addContent(
					builder.getRules().contentRules(),
					new ClauseStatementVisitor(p.getContext()),
					bracesBlock,
					braces);
		}

		return null;
	}

	@Override
	public Void visitDeclarator(DeclaratorNode declarator, StatementsAccess p) {

		final ClauseAccess builder =
				buildOverrider(this.declaration, declarator, p);

		if (builder != null) {
			declare(builder);
			builder.get().build();
		}

		return null;
	}

	@Override
	public Void visitSelfAssignment(
			SelfAssignmentNode assignment,
			StatementsAccess p) {

		final ExpressionNode value = assignment.getValue();

		if (value == null) {
			return null;
		}

		final ClauseAccess builder = builder(p, this.declaration);

		if (builder == null) {
			return null;
		}

		builder.get().assignment();
		buildExpression(builder, value, CLAUSE_EXPRESSION_VISITOR);

		return null;
	}

	@Override
	protected Void visitExpression(
			ExpressionNode expression,
			StatementsAccess p) {

		final ClauseAccess builder = builder(p, this.declaration);

		if (builder != null) {
			buildExpression(builder, expression, CLAUSE_EXPRESSION_VISITOR);
		}

		return null;
	}

	@Override
	protected Void visitStatement(
			StatementNode statement,
			StatementsAccess p) {
		invalidClauseContent(getLogger(), statement);
		return null;
	}

	private final CompilerLogger getLogger() {
		return this.declaration.getContext().getLogger();
	}

	private final ClauseAccess declare(ClauseAccess builder) {
		return ClauseInterpreter.declare(builder, this.node);
	}

	private ClauseAccess builder(
			StatementsAccess statements,
			ClauseDeclaration declaration) {

		final ClauseBuilder builder = statements.get().clause(declaration);

		if (builder == null) {
			return null;
		}

		return declare(new ClauseAccess(statements.getRules(), builder));
	}

	private void buildExpression(
			ClauseAccess builder,
			ExpressionNode expression,
			ClauseExpressionVisitor visitor) {

		final ClauseAccess result = expression.accept(
				visitor,
				builder);

		if (result != null) {
			result.get().build();
		}
	}

}
