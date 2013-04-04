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
package org.o42a.compiler.ip.clause;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.core.member.clause.ClauseDeclaration;


enum ClauseVisibility {

	NAMED_CLAUSE() {
		@Override
		public ClauseDeclaration applyTo(ClauseDeclaration declaration) {
			return declaration;
		}
	},

	IMPLICIT_CLAUSE() {

		@Override
		public ClauseDeclaration applyTo(ClauseDeclaration declaration) {
			return declaration.implicit();
		}

	},

	INTERNAL_CLAUSE() {

		@Override
		public ClauseDeclaration applyTo(ClauseDeclaration declaration) {
			return declaration.internal();
		}

	};

	private static final VisibilityVisitor VISIBILITY_VISITOR =
			new VisibilityVisitor();

	public static ClauseVisibility clauseVisibilityByName(MemberRefNode ref) {

		final ExpressionNode owner = ref.getOwner();

		if (owner == null) {
			return NAMED_CLAUSE;
		}

		return clauseVisibilityByPrefix(owner);
	}

	public static ClauseVisibility clauseVisibilityByPrefix(
			ExpressionNode prefix) {
		return prefix.accept(VISIBILITY_VISITOR, null);
	}

	public abstract ClauseDeclaration applyTo(ClauseDeclaration declaration);

	private static final class VisibilityVisitor
			extends AbstractExpressionVisitor<ClauseVisibility, Void> {

		@Override
		public ClauseVisibility visitScopeRef(ScopeRefNode ref, Void p) {
			switch (ref.getType()) {
			case IMPLIED:
				return IMPLICIT_CLAUSE;
			case SELF:
				return INTERNAL_CLAUSE;
			case ROOT:
			case LOCAL:
			case ANONYMOUS:
			case PARENT:
			case MACROS:
			}
			return null;
		}

		@Override
		protected ClauseVisibility visitExpression(
				ExpressionNode expression,
				Void p) {
			return null;
		}

}

}
