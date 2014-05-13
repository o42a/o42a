/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.file;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ParentRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.util.string.Name;


final class SectionTitlePathValidator
		implements ExpressionNodeVisitor<Object, Integer> {

	private final Name[] path;

	SectionTitlePathValidator(Name[] path) {
		this.path = path;
	}

	@Override
	public Object visitScopeRef(ScopeRefNode ref, Integer p) {
		if (p >= 0) {
			return null;
		}
		return Boolean.TRUE;
	}

	@Override
	public Object visitParentRef(ParentRefNode ref, Integer p) {
		if (p != 0) {
			return null;
		}
		if (!this.path[p].is(ref.getName().getName())) {
			return null;
		}
		return Boolean.TRUE;
	}

	@Override
	public Object visitMemberRef(MemberRefNode ref, Integer p) {
		if (p < 0) {
			return null;
		}
		if (!this.path[p].is(ref.getName().getName())) {
			return null;
		}

		final ExpressionNode owner = ref.getOwner();

		if (owner != null) {
			return owner.accept(this, p - 1);
		}

		return p != 0 ? null : Boolean.TRUE;
	}

	@Override
	public Object visitExpression(ExpressionNode expression, Integer p) {
		return null;
	}

}
