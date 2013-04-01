/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.AccessRules.ACCESS_FROM_DEFINITION;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.compiler.ip.ref.AccessDistributor;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;


final class PhrasePrefixVisitor
		extends AbstractExpressionVisitor<ClauseBuilder, ClauseBuilder> {

	static final PhrasePrefixVisitor PHRASE_PREFIX_VISITOR =
			new PhrasePrefixVisitor();

	private PhrasePrefixVisitor() {
	}

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

		final AccessDistributor distributor =
				ACCESS_FROM_DEFINITION.distribute(p.distribute());
		final AscendantsDefinition ascendantsDefinition =
				CLAUSE_DEF_IP.typeIp().parseAscendants(ascendants, distributor);

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

		final AccessDistributor distributor =
				ACCESS_FROM_DEFINITION.distribute(p.distribute());
		final Ref ancestor = expression.accept(
				CLAUSE_DEF_IP.targetExVisitor(),
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
