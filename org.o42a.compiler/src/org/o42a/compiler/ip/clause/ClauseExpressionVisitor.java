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
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.util.string.Name;


class ClauseExpressionVisitor
		extends AbstractExpressionVisitor<ClauseAccess, ClauseAccess> {

	static final ClauseExpressionVisitor CLAUSE_EXPRESSION_VISITOR =
			new ClauseExpressionVisitor();

	private static final Name PREFIX_NAME =
			CASE_INSENSITIVE.canonicalName("prefix");

	@Override
	public ClauseAccess visitMemberRef(MemberRefNode ref, ClauseAccess p) {
		if (ref.getMembership() == null) {

			final Name localName = localName(ref);

			if (PREFIX_NAME.is(localName)) {
				p.get().setSubstitution(PREFIX_SUBSITUTION);
				return p;
			}
		}

		return super.visitMemberRef(ref, p);
	}

	@Override
	public ClauseAccess visitAscendants(
			AscendantsNode ascendants,
			ClauseAccess p) {

		final AscendantsDefinition ascendantsDefinition =
				CLAUSE_DEF_IP.typeIp().parseAscendants(
						ascendants,
						p.distributeAccess());

		if (ascendantsDefinition == null) {
			return null;
		}

		p.get().setAscendants(ascendantsDefinition);

		return p;
	}

	@Override
	public ClauseAccess visitPhrase(PhraseNode phrase, ClauseAccess p) {

		final PhrasePartNode[] parts = phrase.getParts();

		if (parts.length == 1) {

			final ParenthesesNode parentheses = extractParentheses(parts[0]);

			if (parentheses != null) {

				phrase.getPrefix().accept(PHRASE_PREFIX_VISITOR, p);
				p.get().setDeclarations(contentBuilder(
						p.getRules(),
						new ClauseStatementVisitor(p.getContext()),
						parentheses));

				return p;
			}
		}

		return visitExpression(phrase, p);
	}

	@Override
	protected ClauseAccess visitExpression(
			ExpressionNode expression,
			ClauseAccess p) {

		final AccessDistributor distributor = p.distributeAccess();
		final Ref ref = expression.accept(
				CLAUSE_DEF_IP.targetExVisitor(),
				distributor);

		if (ref == null) {
			return null;
		}

		p.get().setAscendants(
				new AscendantsDefinition(ref, distributor, ref.toTypeRef()));

		return p;
	}

}
