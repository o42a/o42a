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
import static org.o42a.compiler.ip.SampleSpecVisitor.parseAscendants;
import static org.o42a.compiler.ip.member.ParenthesesVisitor.extractParentheses;
import static org.o42a.compiler.ip.member.PhrasePrefixVisitor.PHRASE_PREFIX_VISITOR;
import static org.o42a.core.member.clause.ClauseSubstitution.PREFIX_SUBSITUTION;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.ast.clause.ClauseNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.util.string.Name;


class ClauseExpressionVisitor
		extends AbstractExpressionVisitor<ClauseBuilder, ClauseBuilder> {

	static final ClauseExpressionVisitor CLAUSE_EXPRESSION_VISITOR =
			new ClauseExpressionVisitor();

	private static final Name PREFIX_NAME =
			CASE_INSENSITIVE.canonicalName("prefix");

	@Override
	public ClauseBuilder visitIntrinsicRef(
			IntrinsicRefNode ref,
			ClauseBuilder p) {
		if (PREFIX_NAME.is(ref.getName().getName())) {
			return p.setSubstitution(PREFIX_SUBSITUTION);
		}
		return super.visitIntrinsicRef(ref, p);
	}

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
	public ClauseBuilder visitPhrase(PhraseNode phrase, ClauseBuilder p) {

		final boolean hasValue = p.getDeclaration().getClauseId().hasValue();
		final ClauseNode[] clauses = phrase.getClauses();

		if (clauses.length == 1) {

			final ParenthesesNode parentheses = extractParentheses(clauses[0]);

			if (parentheses != null) {

				final ClauseBuilder prefixed =
						phrase.getPrefix().accept(PHRASE_PREFIX_VISITOR, p);

				return prefixed.setDeclarations(contentBuilder(
						new ClauseStatementVisitor(p.getContext()),
						parentheses));
			} else if (hasValue) {
				p.getContext().getLogger().invalidClauseContent(phrase);
				return null;
			}
		} else if (hasValue) {
			p.getContext().getLogger().invalidClauseContent(phrase);
			return null;
		}

		return visitExpression(phrase, p);
	}

	@Override
	protected ClauseBuilder visitExpression(
			ExpressionNode expression,
			ClauseBuilder p) {

		final Distributor distributor = p.distribute();
		final Ref ref = expression.accept(
				CLAUSE_DEF_IP.targetExVisitor(),
				distributor);

		if (ref == null) {
			return null;
		}

		return p.setAscendants(
				new AscendantsDefinition(ref, distributor, ref.toTypeRef()));
	}

}
