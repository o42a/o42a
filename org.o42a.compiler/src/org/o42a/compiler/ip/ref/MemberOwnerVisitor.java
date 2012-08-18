/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.OwnerVisitor.MACROS_PATH;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.core.Distributor;
import org.o42a.util.log.LogInfo;


final class MemberOwnerVisitor
		extends AbstractExpressionVisitor<Owner, Distributor> {

	private final OwnerVisitor visitor;
	private LogInfo macroExpansion;

	MemberOwnerVisitor(OwnerVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public Owner visitScopeRef(ScopeRefNode ref, Distributor p) {
		if (ref.getType() == ScopeType.MACROS) {
			this.macroExpansion = ref;
			return this.visitor
					.owner(MACROS_PATH.bind(location(p, ref), p.getScope())
					.target(p));
		}
		return super.visitScopeRef(ref, p);
	}

	@Override
	public Owner visitMemberRef(MemberRefNode ref, Distributor p) {
		return this.visitor.memberRef(ref, p, this);
	}

	@Override
	public Owner visitAdapterRef(AdapterRefNode ref, Distributor p) {
		return this.visitor.adapterRef(ref, p, this);
	}

	@Override
	public Owner visitBodyRef(BodyRefNode ref, Distributor p) {
		return this.visitor.bodyRef(ref, p, this);
	}

	@Override
	public Owner visitDeref(DerefNode ref, Distributor p) {
		return this.visitor.deref(ref, p, this);
	}

	@Override
	protected Owner visitExpression(ExpressionNode expression, Distributor p) {
		return this.visitor.visitExpression(expression, p);
	}

	final Owner expandMacro(Owner owner) {
		if (owner == null) {
			return null;
		}
		if (this.macroExpansion == null) {
			return owner;
		}
		return owner.expandMacro(this.macroExpansion);
	}

}
