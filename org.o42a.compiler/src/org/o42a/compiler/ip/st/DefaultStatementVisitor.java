/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import static org.o42a.compiler.ip.clause.ClauseInterpreter.clause;
import static org.o42a.compiler.ip.field.FieldInterpreter.field;
import static org.o42a.compiler.ip.st.LocalInterpreter.local;
import static org.o42a.compiler.ip.st.StInterpreter.addContent;

import org.o42a.ast.clause.ClauseDeclaratorNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.statement.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.*;
import org.o42a.util.string.Name;


public class DefaultStatementVisitor extends StatementVisitor {

	public DefaultStatementVisitor(Interpreter ip, CompilerContext context) {
		super(ip, context);
	}

	@Override
	public Void visitParentheses(
			ParenthesesNode parentheses,
			StatementsAccess p) {

		final Block block = p.get().parentheses(location(p, parentheses));

		addContent(p.getRules(), this, block, parentheses);

		return null;
	}

	@Override
	public Void visitBraces(BracesNode braces, StatementsAccess p) {

		final ImperativeBlock block = p.get().braces(location(p, braces));

		if (block == null) {
			return null;
		}

		addContent(p.getRules(), this, block, braces);

		return null;
	}

	@Override
	public Void visitNamedBlock(NamedBlockNode namedBlock, StatementsAccess p) {

		final ImperativeBlock block = p.get().braces(
				location(p, namedBlock.getName()),
				namedBlock.getName().getName());

		if (block == null) {
			return null;
		}

		addContent(p.getRules(), this, block, namedBlock.getBlock());

		return null;
	}

	@Override
	public Void visitAssignment(AssignmentNode assignment, StatementsAccess p) {

		final AssignableNode destinationNode = assignment.getDestination();

		if (!validateAssignment(assignment)) {
			return null;
		}

		final LocalNode local = destinationNode.toLocal();

		if (local != null) {

			final StatementsAccess statements = addLocalBlock(p, local);

			new LocalStatementVisitor(this)
			.addLocalAssignment(statements, assignment, local);

			return null;
		}

		final Ref destination = destinationNode.toExpression().accept(
				ip().expressionVisitor(),
				p.nextDistributor());

		addAssignment(p, assignment, destination);

		return null;
	}

	@Override
	public Void visitReturn(ReturnNode ret, StatementsAccess p) {

		final ExpressionNode valueNode = ret.getValue();

		if (valueNode == null) {
			return null;
		}

		final AccessDistributor distributor = p.nextDistributor();
		final Ref value =
				valueNode.accept(ip().expressionVisitor(), distributor);

		if (value == null) {
			return null;
		}

		switch (ret.getPrefix().getType()) {
		case RETURN_VALUE:
			p.get().returnValue(location(p, ret.getPrefix()), value);
			return null;
		case YIELD_VALUE:
			p.get().yield(location(p, ret.getPrefix()), value);
			return null;
		}

		throw new UnsupportedOperationException(
				"Unsupported return operator: " + ret.getPrefix().getType());
	}

	@Override
	public Void visitFlow(FlowNode flow, StatementsAccess p) {

		final Name name = flow.getName().getName();
		final FlowBlock block =
				p.get().flow(location(p, flow), name, name);

		if (block != null) {
			addContent(p.getRules(), this, block, flow.getBlock());
		}

		return null;
	}

	@Override
	public Void visitDeclarator(DeclaratorNode declarator, StatementsAccess p) {
		if (local(ip(), getContext(), p, declarator)) {
			return null;
		}

		field(ip(), getContext(), declarator, p);

		return null;
	}

	@Override
	public Void visitLocalScope(LocalScopeNode scope, StatementsAccess p) {
		if (!validateLocalScope(scope)) {
			return null;
		}

		final StatementsAccess statements = addLocalBlock(p, scope.getLocal());

		new LocalStatementVisitor(this).addLocalScope(statements, scope);

		return null;
	}

	@Override
	public Void visitClauseDeclarator(
			ClauseDeclaratorNode declarator,
			StatementsAccess p) {
		clause(getContext(), declarator, p);
		return null;
	}

	@Override
	protected Void visitExpression(
			ExpressionNode expression,
			StatementsAccess p) {

		final AccessDistributor distributor = p.nextDistributor();
		final Ref ref = expression.accept(ip().expressionVisitor(), distributor);

		if (ref != null) {
			p.get().expression(ref);
		}

		return null;
	}

	private static StatementsAccess addLocalBlock(
			StatementsAccess statements,
			LocalNode local) {

		final Location blockLocation;

		if (local.getName() != null) {
			blockLocation = location(statements, local.getName());
		} else {
			blockLocation = location(statements, local.getSeparator());
		}

		final Statements alt =
				statements.get()
				.parentheses(blockLocation)
				.declare(blockLocation)
				.alternative(blockLocation);

		return statements.getRules().statements(alt);
	}

}
