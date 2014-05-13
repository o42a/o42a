/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import static org.o42a.compiler.ip.clause.NameExtractor.extractName;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;


final class BoundNameVisitor
		implements ExpressionNodeVisitor<BoundName, CompilerContext> {

	static final BoundNameVisitor BOUND_NAME_VISITOR =
			new BoundNameVisitor();

	private BoundNameVisitor() {
	}

	@Override
	public BoundName visitScopeRef(ScopeRefNode ref, CompilerContext p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return new BoundName(new Location(p, ref), null, true);
		}
		return visitRef(ref, p);
	}

	@Override
	public BoundName visitMemberRef(MemberRefNode ref, CompilerContext p) {
		return new BoundName(new Location(p, ref), extractName(p, ref), true);
	}

	@Override
	public BoundName visitUnary(UnaryNode expression, CompilerContext p) {

		final Location location = new Location(p, expression);
		final ExpressionNode operand = expression.getOperand();

		if (operand == null) {
			return new BoundName(location, null, false);
		}

		return new BoundName(location, extractName(p, operand), false);
	}

	@Override
	public BoundName visitExpression(
			ExpressionNode expression,
			CompilerContext p) {
		p.getLogger().error(
				"invalid_bound_name",
				expression,
				"Invalid interval bound");
		return new BoundName(new Location(p, expression), null, true);
	}

}
