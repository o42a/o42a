/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
import static org.o42a.compiler.ip.Interpreter.location;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;


final class PhrasePrefixVisitor
		implements ExpressionNodeVisitor<ClauseAccess, ClauseAccess> {

	static final PhrasePrefixVisitor PHRASE_PREFIX_VISITOR =
			new PhrasePrefixVisitor();

	private PhrasePrefixVisitor() {
	}

	@Override
	public ClauseAccess visitScopeRef(ScopeRefNode ref, ClauseAccess p) {
		if (ref.getType() != ScopeType.IMPLIED) {
			return visitRef(ref, p);
		}

		p.get().setAscendants(new AscendantsDefinition(
				location(p, ref),
				p.distribute()));

		return p;
	}

	@Override
	public ClauseAccess visitExpression(
			ExpressionNode expression,
			ClauseAccess p) {

		final AccessDistributor distributor = p.distributeAccess();
		final Ref ancestor = expression.accept(
				CLAUSE_DEF_IP.expressionVisitor(),
				distributor);

		if (ancestor == null) {
			p.get().setAscendants(new AscendantsDefinition(
					location(p, expression),
					distributor));
		} else {
			p.get().setAscendants(
					new AscendantsDefinition(
							ancestor,
							distributor,
							ancestor.toTypeRef()));
		}

		return p;
	}

}
