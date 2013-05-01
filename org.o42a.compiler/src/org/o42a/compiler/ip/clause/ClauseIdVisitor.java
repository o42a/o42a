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
import static org.o42a.compiler.ip.clause.ClauseInterpreter.invalidClauseName;
import static org.o42a.compiler.ip.clause.ClauseVisibility.clauseVisibilityByName;
import static org.o42a.compiler.ip.clause.NameExtractor.extractName;
import static org.o42a.compiler.ip.clause.NameExtractor.extractNameOrImplied;
import static org.o42a.core.member.clause.ClauseDeclaration.anonymousClauseDeclaration;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;
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
				bracketsClauseId(brackets));
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

		final Name name = extractNameOrImplied(
				p.getContext(),
				unary.getOperand());

		return clauseDeclaration(location(p, unary), p, name, clauseId);
	}

	@Override
	public ClauseDeclaration visitBinary(BinaryNode binary, Distributor p) {

		final ClauseId clauseId = binaryClauseId(binary);

		if (clauseId == null) {
			return super.visitBinary(binary, p);
		}

		final Name name = extractNameOrImplied(
				p.getContext(),
				binary.getLeftOperand());

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

		final Name name;
		final ExpressionNode expression =
				assignment.getDestination().toExpression();

		if (expression == null) {
			invalidClauseName(p.getContext(), assignment.getDestination());
			name = null;
		} else {
			name = extractNameOrImplied(p.getContext(), expression);
		}

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
		return IntervalInterpreter.intervalClauseDeclaration(interval, p);
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

	private static BracketsNode rowFromBrackets(BracketsNode brackets) {

		final ArgumentNode[] arguments = brackets.getArguments();

		if (arguments.length == 0) {
			return null;
		}
		if (arguments.length != 1) {
			return null;
		}

		final ArgumentNode argument = arguments[0];

		if (argument.isInitializer()) {
			return null;
		}

		final ExpressionNode value = argument.getValue();

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
			invalidClauseName(context, brackets);
			return null;
		}

		final ExpressionNode value = arguments[0].getValue();

		if (value == null) {
			return null;
		}

		return extractName(context, value);
	}

	private static ClauseId bracketsClauseId(BracketsNode brackets) {

		final ArgumentNode[] arguments = brackets.getArguments();

		if (arguments.length != 0 && arguments[0].isInitializer()) {
			return ClauseId.INITIALIZER;
		}

		return ClauseId.ARGUMENT;
	}

	private static Name nameFromBlock(
			CompilerContext context,
			BlockNode<?> block) {

		final SentenceNode[] sentences = block.getContent();

		if (sentences.length != 1) {
			if (sentences.length != 0) {
				invalidClauseName(context, block);
			}
			return null;
		}

		final SentenceNode sentence = sentences[0];

		if (sentence.getMark() != null) {
			invalidClauseName(context, block);
			return null;
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length != 1) {
			if (disjunction.length != 0) {
				invalidClauseName(context, block);
			}
			return null;
		}

		final SerialNode[] conjunction = disjunction[0].getConjunction();

		if (conjunction.length != 1) {
			if (conjunction.length != 0) {
				invalidClauseName(context, block);
			}
			return null;
		}

		final StatementNode statement = conjunction[0].getStatement();

		if (statement == null) {
			return null;
		}

		return extractName(context, statement);
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
