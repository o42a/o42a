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

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.RefNode;
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

	public static ClauseVisibility clauseVisibilityByName(MemberRefNode ref) {

		final ExpressionNode owner = ref.getOwner();

		if (owner == null) {
			return NAMED_CLAUSE;
		}

		return clauseVisibilityByPrefix(owner);
	}

	public static ClauseVisibility clauseVisibilityByPrefix(
			ExpressionNode prefix) {

		final RefNode ref = prefix.toRef();

		if (ref == null) {
			return null;
		}

		final ScopeRefNode scopeRef = ref.toScopeRef();

		if (scopeRef == null) {
			return null;
		}

		switch (scopeRef.getType()) {
		case IMPLIED:
			return IMPLICIT_CLAUSE;
		case SELF:
			return INTERNAL_CLAUSE;
		case MODULE:
		case ROOT:
		case LOCAL:
		case ANONYMOUS:
		case PARENT:
		case MACROS:
		}
		return null;
	}

	public abstract ClauseDeclaration applyTo(ClauseDeclaration declaration);

}
