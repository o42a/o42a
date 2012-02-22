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

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DEF_IP;
import static org.o42a.compiler.ip.Interpreter.contentBuilder;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.SampleSpecVisitor.parseAscendants;

import org.o42a.ast.clause.AbstractClauseVisitor;
import org.o42a.ast.clause.ClauseNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;


class ClauseExpressionVisitor
		extends AbstractExpressionVisitor<ClauseBuilder, ClauseBuilder> {

	static final ClauseExpressionVisitor CLAUSE_EXPRESSION_VISITOR =
			new ClauseExpressionVisitor();

	static final PhrasePrefixVisitor PHRASE_PREFIX_VISITOR =
			new PhrasePrefixVisitor();
	static final PhraseDeclarationsVisitor PHRASE_DECLARATIONS_VISITOR =
			new PhraseDeclarationsVisitor();

	@Override
	public ClauseBuilder visitAscendants(
			AscendantsNode ascendants,
			ClauseBuilder p) {

		final AscendantsDefinition ascendantsDefinition =
				parseAscendants(CLAUSE_DEF_IP, ascendants, p.distribute());

		if (ascendantsDefinition == null) {
			return null;
		}

		return p.setAscendants(ascendantsDefinition);
	}

	@Override
	public ClauseBuilder visitPhrase(PhraseNode node, ClauseBuilder p) {

		final ClauseNode[] clauses = node.getClauses();

		if (clauses.length != 1) {
			p.getContext().getLogger().invalidClauseContent(node);
			return null;
		}

		final ClauseBuilder prefixed =
				node.getPrefix().accept(PHRASE_PREFIX_VISITOR, p);

		if (prefixed == null) {
			return null;
		}

		return clauses[0].accept(PHRASE_DECLARATIONS_VISITOR, prefixed);
	}

	@Override
	protected ClauseBuilder visitExpression(
			ExpressionNode expression,
			ClauseBuilder p) {

		final Ref ref = expression.accept(
				CLAUSE_DEF_IP.expressionVisitor(),
				p.distribute());

		if (ref == null) {
			return null;
		}

		return p.setAscendants(
				new AscendantsDefinition(
						location(p, expression),
						p.distribute(),
						ref.toTypeRef()));
	}

	private static final class PhrasePrefixVisitor
			extends AbstractExpressionVisitor<ClauseBuilder, ClauseBuilder> {

		@Override
		public ClauseBuilder visitScopeRef(ScopeRefNode ref, ClauseBuilder p) {
			if (ref.getType() != ScopeType.IMPLIED) {
				return super.visitScopeRef(ref, p);
			}

			return p.setAscendants(new AscendantsDefinition(
					location(p, ref),
					p.distribute()));
		}

		@Override
		public ClauseBuilder visitAscendants(
				AscendantsNode ascendants,
				ClauseBuilder p) {

			final Distributor distributor = p.distribute();
			final AscendantsDefinition ascendantsDefinition =
					parseAscendants(CLAUSE_DEF_IP, ascendants, distributor);

			if (ascendantsDefinition == null) {
				return p.setAscendants(new AscendantsDefinition(
						location(p, ascendants),
						distributor));
			}

			return p.setAscendants(ascendantsDefinition);
		}

		@Override
		protected ClauseBuilder visitExpression(
				ExpressionNode expression,
				ClauseBuilder p) {

			final Distributor distributor = p.distribute();
			final Ref ancestor = expression.accept(
					CLAUSE_DEF_IP.expressionVisitor(),
					distributor);

			if (ancestor == null) {
				return p.setAscendants(new AscendantsDefinition(
						location(p, expression),
						distributor));
			}

			return p.setAscendants(
					new AscendantsDefinition(
							ancestor,
							distributor,
							ancestor.toTypeRef()));
		}

	}

	private static final class PhraseDeclarationsVisitor
			extends AbstractClauseVisitor<ClauseBuilder, ClauseBuilder> {

		@Override
		public ClauseBuilder visitParentheses(
				ParenthesesNode parentheses,
				ClauseBuilder p) {
			return p.setDeclarations(contentBuilder(
					new ClauseStatementVisitor(p.getContext()),
					parentheses));
		}

		@Override
		protected ClauseBuilder visitClause(
				ClauseNode clause,
				ClauseBuilder p) {
			p.getContext().getLogger().invalidClauseContent(clause);
			return null;
		}

	}

}
