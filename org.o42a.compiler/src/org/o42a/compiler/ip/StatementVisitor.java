/*
    Compiler
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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.Interpreter.addContent;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.member.ClauseInterpreter.clause;
import static org.o42a.compiler.ip.member.FieldInterpreter.field;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.module.InclusionNode;
import org.o42a.ast.statement.*;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.st.sentence.Statements;


public class StatementVisitor
		extends AbstractStatementVisitor<Void, Statements<?>> {

	private final Interpreter ip;
	private final CompilerContext context;

	public StatementVisitor(Interpreter ip, CompilerContext context) {
		this.ip = ip;
		this.context = context;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final ExpressionNodeVisitor<Ref, Distributor> expressionVisitor() {
		return ip().expressionVisitor();
	}

	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Void visitParentheses(ParenthesesNode parentheses, Statements<?> p) {

		final Block<?> block = p.parentheses(location(p, parentheses));

		addContent(this, block, parentheses);

		return null;
	}

	@Override
	public Void visitBraces(BracesNode braces, Statements<?> p) {

		final Block<Imperatives> block = p.braces(location(p, braces));

		if (block == null) {
			return null;
		}

		addContent(this, block, braces);

		return null;
	}

	@Override
	public Void visitNamedBlock(NamedBlockNode namedBlock, Statements<?> p) {

		final Block<Imperatives> block = p.braces(
				location(p, namedBlock.getName()),
				namedBlock.getName().getName());

		if (block == null) {
			return null;
		}

		addContent(this, block, namedBlock.getBlock());

		return null;
	}

	@Override
	public Void visitAssignment(AssignmentNode assignment, Statements<?> p) {

		final ExpressionNode destinationNode = assignment.getDestination();
		final ExpressionNode valueNode = assignment.getValue();

		if (valueNode == null || destinationNode == null) {
			return null;
		}

		final Distributor distributor = p.nextDistributor();
		final Ref destination =
				destinationNode.accept(expressionVisitor(), distributor);
		final Ref value = valueNode.accept(expressionVisitor(), distributor);

		if (destination == null || value == null) {
			return null;
		}

		p.assign(location(p, assignment.getOperator()), destination, value);

		return null;
	}

	@Override
	public Void visitSelfAssignment(
			SelfAssignmentNode assignment,
			Statements<?> p) {

		final ExpressionNode valueNode = assignment.getValue();

		if (valueNode == null) {
			return null;
		}

		final Distributor distributor = p.nextDistributor();
		final Ref value = valueNode.accept(expressionVisitor(), distributor);

		if (value == null) {
			return null;
		}

		p.selfAssign(location(p, assignment.getPrefix()), value);

		return null;
	}

	@Override
	public Void visitDeclarator(DeclaratorNode declarator, Statements<?> p) {
		field(ip(), getContext(), declarator, p);
		return null;
	}

	@Override
	public Void visitClauseDeclarator(
			ClauseDeclaratorNode declarator,
			Statements<?> p) {
		clause(getContext(), declarator, p);
		return null;
	}

	@Override
	public Void visitEllipsis(EllipsisNode ellipsis, Statements<?> p) {

		final NameNode target = ellipsis.getTarget();

		p.ellipsis(
				location(p, ellipsis),
				target != null ? target.getName() : null);

		return null;
	}

	@Override
	public Void visitInclusion(InclusionNode inclusion, Statements<?> p) {

		final NameNode tag = inclusion.getTag();

		if (tag != null) {
			p.include(location(p, inclusion), tag.getName());
		}

		return null;
	}

	@Override
	protected Void visitExpression(ExpressionNode expression, Statements<?> p) {

		final Distributor distributor = p.nextDistributor();
		final Ref ref = expression.accept(expressionVisitor(), distributor);

		if (ref != null) {
			p.expression(ref);
		}

		return null;
	}

	@Override
	protected Void visitStatement(StatementNode statement, Statements<?> p) {
		p.getLogger().invalidStatement(statement);
		return null;
	}

}
