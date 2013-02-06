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
package org.o42a.compiler.ip.member;

import static org.o42a.ast.phrase.IntervalBracket.LEFT_CLOSED_BRACKET;
import static org.o42a.ast.phrase.IntervalBracket.RIGHT_CLOSED_BRACKET;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.signType;
import static org.o42a.compiler.ip.member.ClauseVisibility.clauseVisibilityByName;
import static org.o42a.core.member.clause.ClauseDeclaration.anonymousClauseDeclaration;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;
import static org.o42a.core.member.clause.ClauseId.*;
import static org.o42a.util.string.Name.caseInsensitiveName;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.StringNode;
import org.o42a.ast.clause.AbstractClauseIdVisitor;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.AbstractStatementVisitor;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.log.LogInfo;
import org.o42a.util.string.Name;


final class ClauseIdVisitor
		extends AbstractClauseIdVisitor<ClauseDeclaration, Distributor> {

	static final ClauseIdVisitor CLAUSE_ID_VISITOR = new ClauseIdVisitor();

	private static final NameExtractor NAME_EXTRACTOR = new NameExtractor();
	private static final NameOrImpliedExtractor NAME_OR_IMPLIED_EXTRACTOR =
			new NameOrImpliedExtractor();
	private static final AsteriskChecker ASTERISK_CHECKER =
			new AsteriskChecker();
	private static final BracketsExtractor BRACKETS_EXTRACTOR =
			new BracketsExtractor();

	private ClauseIdVisitor() {
	}

	@Override
	public ClauseDeclaration visitScopeRef(ScopeRefNode ref, Distributor p) {
		if (ref.getType() != ScopeType.IMPLIED) {
			return super.visitScopeRef(ref, p);
		}
		return anonymousClauseDeclaration(
				location(p, ref),
				p).implicit();
	}

	@Override
	public ClauseDeclaration visitMemberRef(MemberRefNode ref, Distributor p) {
		if (ref.getDeclaredIn() != null) {
			p.getLogger().prohibitedDeclaredIn(ref.getDeclaredIn());
		}

		final ClauseVisibility visibility = clauseVisibilityByName(ref);

		if (visibility == null) {
			p.getLogger().invalidDeclaration(ref);
			return null;
		}

		final NameNode name = ref.getName();
		final ClauseDeclaration declaration;

		if (name == null) {
			declaration = anonymousClauseDeclaration(
					location(p, ref),
					p);
		} else {
			declaration = clauseDeclaration(
					location(p, ref),
					p,
					name.getName(),
					ClauseId.NAME);
		}

		return visibility.applyTo(declaration);
	}

	@Override
	public ClauseDeclaration visitBrackets(
			BracketsNode brackets,
			Distributor p) {

		final BracketsNode row = rowFromBrackets(brackets);

		if (row != null) {
			return clauseDeclaration(
					location(p, brackets),
					p,
					nameFromBrackets(p.getContext(), row),
					ClauseId.ROW);
		}

		return clauseDeclaration(
				location(p, brackets),
				p,
				nameFromBrackets(p.getContext(), brackets),
				ClauseId.ARGUMENT);
	}

	@Override
	public ClauseDeclaration visitString(
			StringNode string,
			Distributor p) {
		if (string.isDoubleQuoted()) {
			return super.visitString(string, p);
		}
		return clauseDeclaration(
				location(p, string),
				p,
				buildClauseName(p.getContext(), string, string.getText()),
				ClauseId.STRING);
	}

	@Override
	public ClauseDeclaration visitBraces(BracesNode braces, Distributor p) {
		return clauseDeclaration(
				location(p, braces),
				p,
				nameFromBlock(p.getContext(), braces),
				ClauseId.IMPERATIVE);
	}

	@Override
	public ClauseDeclaration visitUnary(UnaryNode unary, Distributor p) {

		final ClauseId clauseId = unaryClauseId(unary);

		if (clauseId == null) {
			return super.visitUnary(unary, p);
		}

		final Name name = unary.getOperand().accept(
				NAME_OR_IMPLIED_EXTRACTOR,
				p.getContext());

		return clauseDeclaration(location(p, unary), p, name, clauseId);
	}

	@Override
	public ClauseDeclaration visitBinary(BinaryNode binary, Distributor p) {

		final ClauseId clauseId = binaryClauseId(binary);

		if (clauseId == null) {
			return super.visitBinary(binary, p);
		}

		final Name name = binary.getLeftOperand().accept(
				NAME_OR_IMPLIED_EXTRACTOR,
				p.getContext());

		final ExpressionNode rightOperand = binary.getRightOperand();

		if (rightOperand != null) {
			rightOperand.accept(ASTERISK_CHECKER, p.getContext());
		}

		return clauseDeclaration(location(p, binary), p, name, clauseId);
	}

	@Override
	public ClauseDeclaration visitAssignment(
			AssignmentNode assignment,
			Distributor p) {

		final Name name = assignment.getDestination().accept(
				NAME_OR_IMPLIED_EXTRACTOR,
				p.getContext());

		return clauseDeclaration(
				location(p, assignment),
				p,
				name,
				ClauseId.ASSIGN);
	}

	@Override
	public ClauseDeclaration visitInterval(
			IntervalNode interval,
			Distributor p) {

		final ExpressionNode bound = intervalBound(interval, p);

		if (bound == null) {
			return null;
		}

		final Name name = bound.accept(
				NAME_OR_IMPLIED_EXTRACTOR,
				p.getContext());

		return clauseDeclaration(
				location(p, interval),
				p,
				name,
				intervalClauseId(interval));
	}

	@Override
	protected ClauseDeclaration visitClauseId(
			ClauseIdNode clauseId,
			Distributor p) {
		p.getLogger().invalidDeclaration(clauseId);
		return null;
	}

	private ClauseId unaryClauseId(UnaryNode unary) {
		switch (unary.getOperator()) {
		case PLUS:
			return ClauseId.PLUS;
		case MINUS:
			return ClauseId.MINUS;
		case IS_TRUE:
		case NOT:
		case MACRO_EXPANSION:
		case VALUE_OF:
		case KEEP_VALUE:
			return null;
		}
		return null;
	}

	private static ClauseId binaryClauseId(BinaryNode binary) {
		switch (binary.getOperator()) {
		case ADD:
			return ClauseId.ADD;
		case SUBTRACT:
			return ClauseId.SUBTRACT;
		case MULTIPLY:
			return ClauseId.MULTIPLY;
		case DIVIDE:
			return ClauseId.DIVIDE;
		case EQUAL:
			return ClauseId.EQUALS;
		case COMPARE:
			return ClauseId.COMPARE;
		case SUFFIX:
			return ClauseId.SUFFIX;
		case GREATER:
		case GREATER_OR_EQUAL:
		case LESS:
		case LESS_OR_EQUAL:
		case NOT_EQUAL:
			return null;
		}
		return null;
	}

	private static void expectedClauseName(
			CompilerContext context,
			LogInfo location) {
		context.getLogger().error(
				"expected_clause_name",
				location,
				"Clause name expected here");
	}

	private static BracketsNode rowFromBrackets(BracketsNode brackets) {

		final ArgumentNode[] arguments = brackets.getArguments();

		if (arguments.length == 0) {
			return null;
		}
		if (arguments.length != 1) {
			return null;
		}

		final ExpressionNode value = arguments[0].getValue();

		if (value == null) {
			return null;
		}

		return value.accept(BRACKETS_EXTRACTOR, null);
	}

	private static Name nameFromBrackets(
			CompilerContext context,
			BracketsNode brackets) {

		final ArgumentNode[] arguments = brackets.getArguments();

		if (arguments.length == 0) {
			return null;
		}
		if (arguments.length != 1) {
			expectedClauseName(context, brackets);
			return null;
		}

		final ExpressionNode value = arguments[0].getValue();

		if (value == null) {
			return null;
		}

		return value.accept(NAME_EXTRACTOR, context);
	}

	private static Name nameFromBlock(
			CompilerContext context,
			BlockNode<?> block) {

		final SentenceNode[] sentences = block.getContent();

		if (sentences.length != 1) {
			if (sentences.length != 0) {
				expectedClauseName(context, block);
			}
			return null;
		}

		final SentenceNode sentence = sentences[0];

		if (sentence.getMark() != null) {
			expectedClauseName(context, block);
			return null;
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length != 1) {
			if (disjunction.length != 0) {
				expectedClauseName(context, block);
			}
			return null;
		}

		final SerialNode[] conjunction = disjunction[0].getConjunction();

		if (conjunction.length != 1) {
			if (conjunction.length != 0) {
				expectedClauseName(context, block);
			}
			return null;
		}

		final StatementNode statement = conjunction[0].getStatement();

		if (statement == null) {
			return null;
		}

		return statement.accept(NAME_EXTRACTOR, context);
	}

	private static Name buildClauseName(
			CompilerContext context,
			LogInfo location,
			String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}

		final Name clauseName = caseInsensitiveName(name);

		if (clauseName.isValid()) {
			return clauseName;
		}

		context.getLogger().error(
				"invalid_clause_name",
				location,
				"Invalid clause name: %s",
				name);

		return null;
	}

	private ExpressionNode intervalBound(IntervalNode interval, Distributor p) {

		final ExpressionNode leftBound = interval.getLeftBound();
		final ExpressionNode rightBound = interval.getRightBound();

		if (leftBound != null) {
			if (rightBound == null) {
				return leftBound;
			}
		} else if (rightBound != null) {
			return rightBound;
		}

		p.getLogger().error(
				"invalid_interval_clause_id",
				interval,
				"Exactly one interval bound (either left or right one)"
						+ " can be specified here");
		return null;
	}

	private ClauseId intervalClauseId(IntervalNode interval) {
		if (interval.getLeftBound() != null) {
			if (interval.getLeftBracket().getType() == LEFT_CLOSED_BRACKET) {
				if (signType(interval.getRightBracket())
						== RIGHT_CLOSED_BRACKET) {
					return CLOSED_INTERVAL_START;
				}
				return RIGHT_OPEN_INTERVAL_START;
			}
			if (signType(interval.getRightBracket()) == RIGHT_CLOSED_BRACKET) {
				return LEFT_OPEN_INTERVAL_START;
			}
			return OPEN_INTERVAL_START;
		}
		if (interval.getLeftBracket().getType() == LEFT_CLOSED_BRACKET) {
			if (signType(interval.getRightBracket())
					== RIGHT_CLOSED_BRACKET) {
				return CLOSED_INTERVAL_END;
			}
			return RIGHT_OPEN_INTERVAL_END;
		}
		if (signType(interval.getRightBracket()) == RIGHT_CLOSED_BRACKET) {
			return LEFT_OPEN_INTERVAL_END;
		}
		return OPEN_INTERVAL_END;
	}

	private static final class NameExtractor
			extends AbstractStatementVisitor<Name, CompilerContext> {

		@Override
		public Name visitMemberRef(MemberRefNode ref, CompilerContext p) {
			if (ref.getDeclaredIn() != null) {
				p.getLogger().prohibitedDeclaredIn(ref.getDeclaredIn());
				return null;
			}
			if (ref.getOwner() != null) {
				expectedClauseName(p, ref);
				return null;
			}

			final NameNode name = ref.getName();

			if (name == null) {
				expectedClauseName(p, ref);
				return null;
			}

			return name.getName();
		}

		@Override
		protected Name visitStatement(
				StatementNode statement,
				CompilerContext p) {
			expectedClauseName(p, statement);
			return null;
		}

	}

	private static final class NameOrImpliedExtractor
			extends AbstractExpressionVisitor<Name, CompilerContext> {

		@Override
		public Name visitScopeRef(ScopeRefNode ref, CompilerContext p) {
			if (ref.getType() == ScopeType.IMPLIED) {
				return null;
			}
			return super.visitScopeRef(ref, p);
		}

		@Override
		public Name visitMemberRef(MemberRefNode ref, CompilerContext p) {
			return ref.accept(NAME_EXTRACTOR, p);
		}

		@Override
		protected Name visitExpression(
				ExpressionNode expression,
				CompilerContext p) {
			expectedClauseName(p, expression);
			return null;
		}

	}

	private static final class AsteriskChecker
			extends AbstractExpressionVisitor<Void, CompilerContext> {

		@Override
		public Void visitScopeRef(ScopeRefNode ref, CompilerContext p) {
			if (ref.getType() == ScopeType.IMPLIED) {
				return null;
			}
			return super.visitScopeRef(ref, p);
		}

		@Override
		protected Void visitExpression(
				ExpressionNode expression,
				CompilerContext p) {
			p.getLogger().error(
					"asterisk_expected",
					p,
					"Asterisk expected here");
			return null;
		}

	}

	private static final class BracketsExtractor
			extends AbstractStatementVisitor<BracketsNode, Object> {

		@Override
		public BracketsNode visitBrackets(
				BracketsNode brackets,
				Object p) {
			return brackets;
		}

		@Override
		protected BracketsNode visitStatement(
				StatementNode statement,
				Object p) {
			return null;
		}

	}

}
