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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.ir.IRUtil.canonicalName;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.AbstractStatementVisitor;
import org.o42a.ast.statement.StatementNode;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseId;


final class PhraseClauseKeyVisitor
		extends AbstractClauseVisitor<ClauseDeclaration, Distributor> {

	private static final NameExtractor NAME_EXTRACTOR = new NameExtractor();

	private final PhraseNode phrase;

	PhraseClauseKeyVisitor(PhraseNode phrase) {
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
		return clauseDeclaration(
				location(p, this.phrase),
				p,
				extractName(p.getContext(), brackets),
				ClauseId.ARGUMENT);
	}

	@Override
	public ClauseDeclaration visitText(TextNode text, Distributor p) {

		final String name = canonicalName(text.getText());

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

	private static String extractName(
			CompilerContext context,
			BracketsNode brackets) {
		final ArgumentNode[] arguments = brackets.getArguments();

		if (arguments.length == 0) {
			return null;
		}
		if (arguments.length != 1) {
			context.getLogger().expectedClauseName(brackets);
			return null;
		}

		final ExpressionNode value = arguments[0].getValue();

		if (value == null) {
			return null;
		}

		return value.accept(NAME_EXTRACTOR, context);
	}

	private static String extractName(
			CompilerContext context,
			BlockNode<?> block) {

		final SentenceNode[] sentences = block.getContent();

		if (sentences.length != 1) {
			if (sentences.length != 0) {
				context.getLogger().expectedClauseName(block);
			}
			return null;
		}

		final SentenceNode sentence = sentences[0];

		if (sentence.getMark() != null) {
			context.getLogger().expectedClauseName(block);
			return null;
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length != 1) {
			if (disjunction.length != 0) {
				context.getLogger().expectedClauseName(block);
			}
			return null;
		}

		final SerialNode[] conjunction = disjunction[0].getConjunction();

		if (conjunction.length != 1) {
			if (conjunction.length != 0) {
				context.getLogger().expectedClauseName(block);
			}
			return null;
		}

		final StatementNode statement = conjunction[0].getStatement();

		if (statement == null) {
			return null;
		}

		return statement.accept(NAME_EXTRACTOR, context);
	}

	private static final class NameExtractor
			extends AbstractStatementVisitor<String, CompilerContext> {

		@Override
		public String visitMemberRef(MemberRefNode ref, CompilerContext p) {
			if (ref.getDeclaredIn() != null) {
				p.getLogger().prohibitedDeclaredIn(ref.getDeclaredIn());
				return null;
			}
			if (ref.getOwner() != null) {
				p.getLogger().expectedClauseName(ref);
				return null;
			}

			final NameNode name = ref.getName();

			if (name == null) {
				p.getLogger().expectedClauseName(ref);
				return null;
			}

			return name.getName();
		}

		@Override
		protected String visitStatement(
				StatementNode statement,
				CompilerContext p) {
			p.getLogger().expectedClauseName(statement);
			return null;
		}

	}

}
