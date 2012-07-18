/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;
import static org.o42a.util.string.Name.caseInsensitiveName;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.clause.AbstractClauseVisitor;
import org.o42a.ast.clause.ClauseNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.AbstractStatementVisitor;
import org.o42a.ast.statement.StatementNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.log.LogInfo;
import org.o42a.util.string.Name;


final class PhraseClauseIdVisitor
		extends AbstractClauseVisitor<ClauseDeclaration, Distributor> {

	private static final NameExtractor NAME_EXTRACTOR = new NameExtractor();
	private static final BracketsExtractor BRACKETS_EXTRACTOR =
			new BracketsExtractor();

	private final PhraseNode phrase;

	PhraseClauseIdVisitor(PhraseNode phrase) {
		this.phrase = phrase;
	}

	@Override
	public ClauseDeclaration visitBraces(BracesNode braces, Distributor p) {
		return clauseDeclaration(
				location(p, this.phrase),
				p,
				extractName(p.getContext(), braces),
				ClauseId.IMPERATIVE);
	}

	@Override
	public ClauseDeclaration visitBrackets(
			BracketsNode brackets,
			Distributor p) {

		final BracketsNode row = extractRow(brackets);

		if (row != null) {
			return clauseDeclaration(
					location(p, this.phrase),
					p,
					extractName(p.getContext(), row),
					ClauseId.ROW);
		}

		return clauseDeclaration(
				location(p, this.phrase),
				p,
				extractName(p.getContext(), brackets),
				ClauseId.ARGUMENT);
	}

	@Override
	public ClauseDeclaration visitText(TextNode text, Distributor p) {
		if (text.isDoubleQuote()) {
			return super.visitText(text, p);
		}

		final Name name = clauseName(p.getContext(), text, text.getText());

		return clauseDeclaration(
				location(p, this.phrase),
				p,
				name.isEmpty() ? null : name,
				ClauseId.STRING);
	}

	@Override
	protected ClauseDeclaration visitClause(
			ClauseNode clause,
			Distributor p) {
		p.getLogger().invalidDeclaration(clause);
		return null;
	}

	private static Name extractName(
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

	private static BracketsNode extractRow(BracketsNode brackets) {

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

	private static void expectedClauseName(
			CompilerContext context,
			LogInfo location) {
		context.getLogger().error(
				"expected_clause_name",
				location,
				"Clause name expected here");
	}

	private static Name extractName(
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

	private Name clauseName(
			CompilerContext context,
			LogInfo location,
			String name) {
		if (name == null) {
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
