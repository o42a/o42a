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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.Interpreter.addContent;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.member.ClauseInterpreter.clause;
import static org.o42a.compiler.ip.member.FieldInterpreter.field;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.statement.*;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.st.sentence.Statements;


public class StatementVisitor
		extends AbstractStatementVisitor<Ref, Statements<?>> {

	private final CompilerContext context;

	public StatementVisitor(CompilerContext context) {
		this.context = context;
	}

	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Ref visitParentheses(ParenthesesNode parentheses, Statements<?> p) {

		final Block<?> block = p.parentheses(location(p, parentheses));

		addContent(this, block, parentheses);

		return null;
	}

	@Override
	public Ref visitBraces(BracesNode braces, Statements<?> p) {

		final Block<Imperatives> block = p.braces(location(p, braces));

		if (block == null) {
			return null;
		}

		addContent(this, block, braces);

		return null;
	}

	@Override
	public Ref visitNamedBlock(NamedBlockNode namedBlock, Statements<?> p) {

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
	public Ref visitSelfAssignment(
			SelfAssignmentNode assignment,
			Statements<?> p) {

		final ExpressionNode value = assignment.getValue();

		if (value != null) {

			final Distributor distributor = p.nextDistributor();

			p.assign(value.accept(EXPRESSION_VISITOR, distributor));
		}

		return null;
	}

	@Override
	public Ref visitDeclarator(DeclaratorNode declarator, Statements<?> p) {
		return field(getContext(), declarator, p);
	}

	@Override
	public Ref visitClauseDeclarator(
			ClauseDeclaratorNode declarator,
			Statements<?> p) {
		return clause(getContext(), declarator, p);
	}

	@Override
	public Ref visitEllipsis(EllipsisNode ellipsis, Statements<?> p) {

		final NameNode target = ellipsis.getTarget();

		p.ellipsis(
				location(p, ellipsis),
				target != null ? target.getName() : null);

		return null;
	}

	@Override
	protected Ref visitExpression(ExpressionNode expression, Statements<?> p) {

		final Distributor distributor = p.nextDistributor();
		final Ref ref = expression.accept(EXPRESSION_VISITOR, distributor);

		if (ref != null) {
			p.expression(ref);
		}

		return null;
	}

	@Override
	protected Ref visitStatement(StatementNode statement, Statements<?> p) {
		p.getLogger().invalidStatement(statement);
		return null;
	}

}
