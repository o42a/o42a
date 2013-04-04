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

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DEF_IP;
import static org.o42a.compiler.ip.clause.ClauseAccessRules.ACCESS_FROM_CLAUSE_CONTENT;
import static org.o42a.compiler.ip.clause.ParenthesesVisitor.extractParentheses;
import static org.o42a.compiler.ip.clause.PhrasePrefixVisitor.PHRASE_PREFIX_VISITOR;
import static org.o42a.compiler.ip.st.LocalInterpreter.localName;
import static org.o42a.compiler.ip.st.StInterpreter.contentBuilder;
import static org.o42a.core.member.clause.ClauseSubstitution.PREFIX_SUBSITUTION;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.compiler.ip.access.AccessDistributor;
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
	public ClauseBuilder visitMemberRef(MemberRefNode ref, ClauseBuilder p) {
		if (ref.getMembership() == null) {

			final Name localName = localName(ref);

			if (PREFIX_NAME.is(localName)) {
				return p.setSubstitution(PREFIX_SUBSITUTION);
			}
		}

		return super.visitMemberRef(ref, p);
	}

	@Override
	public ClauseBuilder visitAscendants(
			AscendantsNode ascendants,
			ClauseBuilder p) {

		final AscendantsDefinition ascendantsDefinition =
				CLAUSE_DEF_IP.typeIp().parseAscendants(
						ascendants,
						ACCESS_FROM_CLAUSE_CONTENT.distribute(p.distribute()));

		if (ascendantsDefinition == null) {
			return null;
		}

		return p.setAscendants(ascendantsDefinition);
	}

	@Override
	public ClauseBuilder visitPhrase(PhraseNode phrase, ClauseBuilder p) {

		final PhrasePartNode[] parts = phrase.getParts();

		if (parts.length == 1) {

			final ParenthesesNode parentheses = extractParentheses(parts[0]);

			if (parentheses != null) {

				final ClauseBuilder prefixed =
						phrase.getPrefix().accept(PHRASE_PREFIX_VISITOR, p);

				return prefixed.setDeclarations(contentBuilder(
						ACCESS_FROM_CLAUSE_CONTENT,
						new ClauseStatementVisitor(p.getContext()),
						parentheses));
			}
		}

		return visitExpression(phrase, p);
	}

	@Override
	protected ClauseBuilder visitExpression(
			ExpressionNode expression,
			ClauseBuilder p) {

		final AccessDistributor distributor =
				ACCESS_FROM_CLAUSE_CONTENT.distribute(p.distribute());
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
