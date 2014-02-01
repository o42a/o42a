/*
    Compiler
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.member.clause.ClauseSubstitution.VALUE_SUBSTITUTION;

import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.core.member.field.AscendantsDefinition;


final class OverriderDefinitionVisitor
		extends ClauseExpressionVisitor {

	static final OverriderDefinitionVisitor OVERRIDER_DEFINITION_VISITOR =
			new OverriderDefinitionVisitor();

	private OverriderDefinitionVisitor() {
	}

	@Override
	public ClauseAccess visitScopeRef(ScopeRefNode ref, ClauseAccess p) {
		if (ref.getType() != ScopeType.IMPLIED) {
			return super.visitScopeRef(ref, p);
		}

		p.get().setAscendants(new AscendantsDefinition(
				location(p, ref),
				p.distribute()));

		return p;
	}

	@Override
	public ClauseAccess visitParentheses(
			ParenthesesNode parentheses,
			ClauseAccess p) {
		if (parentheses.getContent().length == 0) {
			p.get().setSubstitution(VALUE_SUBSTITUTION);
			return p;
		}
		return super.visitParentheses(parentheses, p);
	}

}
