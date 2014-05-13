/*
    Compiler
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
package org.o42a.compiler.ip.st;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.st.LocalInterpreter.local;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.statement.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.phrase.PhraseBuilder;
import org.o42a.compiler.ip.st.assignment.AssignmentStatement;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.log.LogInfo;
import org.o42a.util.string.Name;


public abstract class StatementVisitor
		implements StatementNodeVisitor<Void, StatementsAccess> {

	public static final Name DESTINATION_LOCAL_NAME =
			CASE_SENSITIVE.canonicalName("LD");
	public static final Name VALUE_LOCAL_NAME =
			CASE_SENSITIVE.canonicalName("LV");

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

	public final CompilerContext getContext() {
		return this.context;
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public Void visitNumber(NumberNode number, StatementsAccess p) {
		return invalidStatement(number);
	}

	@Override
	public Void visitText(TextNode text, StatementsAccess p) {
		return invalidStatement(text);
	}

	@Override
	public Void visitBrackets(BracketsNode brackets, StatementsAccess p) {
		return invalidStatement(brackets);
	}

	@Override
	public Void visitParentheses(
			ParenthesesNode parentheses,
			StatementsAccess p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return visitStatement(parentheses, p);
	}

	@Override
	public Void visitStatement(StatementNode statement, StatementsAccess p) {
		return invalidStatement(statement);
	}

	Void invalidStatement(LogInfo location) {
		invalidStatement(getLogger(), location);
		return null;
	}

	void addAssignment(
			StatementsAccess statements,
			AssignmentNode assignment,
			Ref destination) {
		if (assignment.getOperator().getType().isBinding()) {
			addBinding(statements, assignment, destination);
		} else {
			addValueAssignment(statements, assignment, destination);
		}
	}

	private void addBinding(
			StatementsAccess statements,
			AssignmentNode assignment,
			Ref destination) {

		final AccessDistributor distributor = statements.nextDistributor();
		final RefBuilder value = assignment.getValue().accept(
				ip().refBuildVisitor(),
				distributor);

		if (destination == null || value == null) {
			return;
		}

		statements.statement(new AssignmentStatement(
				assignment,
				distributor,
				destination,
				value));
	}

	private void addValueAssignment(
			StatementsAccess statements,
			AssignmentNode assignment,
			Ref destination) {

		final Location location =
				location(destination, assignment.getOperator());
		final StatementsAccess st = new StatementsAccess(
				statements.getRules(),
				statements.get()
				.parentheses(location)
				.declare(location)
				.alternative(location));
		final Ref dest;
		final Local local;

		if (!assignment.getOperator().getType().isCombined()) {
			dest = destination;
			local = local(
					ip(),
					location.getContext(),
					st,
					location.getLocation(),
					VALUE_LOCAL_NAME,
					assignment.getValue());
		} else {

			final Local destLocal = st.get().local(
					destination,
					DESTINATION_LOCAL_NAME,
					destination);

			dest = destLocal.toRef();

			final PhraseBuilder phrase = new PhraseBuilder(
					ip(),
					location,
					st.nextDistributor(),
					null);
			final Ref value = phrase.binary(dest, assignment).toRef();

			local = st.get().local(value, VALUE_LOCAL_NAME, value);
		}

		if (local == null) {
			return;
		}

		st.statement(new AssignmentStatement(
				assignment,
				st.nextDistributor(),
				dest,
				local));
	}

}
