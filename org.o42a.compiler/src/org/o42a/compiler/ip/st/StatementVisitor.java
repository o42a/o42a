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
package org.o42a.compiler.ip.st;

import static org.o42a.compiler.ip.Interpreter.unwrap;

import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.statement.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.st.assignment.AssignmentStatement;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogInfo;


public abstract class StatementVisitor
		extends AbstractStatementVisitor<Void, StatementsAccess> {

	static void invalidStatement(CompilerLogger logger, LogInfo location) {
		logger.error(
				"invalid_statement",
				location,
				"Not a valid statement");
	}

	static boolean validateAssignment(AssignmentNode assignment) {
		return assignment.getValue() != null
				&& assignment.getDestination() != null;
	}

	static boolean validateLocalScope(LocalScopeNode scope) {
		return scope.getContent() != null;
	}

	private final Interpreter ip;
	private final CompilerContext context;

	public StatementVisitor(Interpreter ip, CompilerContext context) {
		this.ip = ip;
		this.context = context;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final
	ExpressionNodeVisitor<Ref, AccessDistributor> expressionVisitor() {
		return ip().targetExVisitor();
	}

	public final CompilerContext getContext() {
		return this.context;
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public Void visitNumber(NumberNode number, StatementsAccess p) {
		invalidStatement(number);
		return null;
	}

	@Override
	public Void visitText(TextNode text, StatementsAccess p) {
		invalidStatement(text);
		return null;
	}

	@Override
	public Void visitBrackets(BracketsNode brackets, StatementsAccess p) {
		invalidStatement(brackets);
		return null;
	}

	@Override
	public Void visitParentheses(
			ParenthesesNode parentheses,
			StatementsAccess p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	protected Void visitStatement(StatementNode statement, StatementsAccess p) {
		invalidStatement(statement);
		return null;
	}

	void invalidStatement(LogInfo location) {
		invalidStatement(getLogger(), location);
	}

	void addAssignment(
			StatementsAccess statements,
			AssignmentNode assignment,
			Ref destination) {

		final AccessDistributor distributor = statements.nextDistributor();
		final Ref value =
				assignment.getValue().accept(expressionVisitor(), distributor);

		if (destination == null || value == null) {
			return;
		}

		statements.statement(
				new AssignmentStatement(
						assignment,
						statements.getRules(),
						destination,
						value));
	}

}
