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
package org.o42a.compiler.ip.ref;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.OwnerVisitor.MACROS_PATH;
import static org.o42a.compiler.ip.ref.owner.Owner.owner;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.util.log.LogInfo;


final class MemberOwnerVisitor
		implements ExpressionNodeVisitor<Owner, AccessDistributor> {

	private final OwnerVisitor visitor;
	private LogInfo macroExpansion;

	MemberOwnerVisitor(OwnerVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public Owner visitScopeRef(ScopeRefNode ref, AccessDistributor p) {
		if (ref.getType() == ScopeType.MACROS) {
			this.macroExpansion = ref;
			return owner(
							p.getAccessRules(),
							MACROS_PATH.bind(location(p, ref), p.getScope())
					.target(p));
		}
		return visitExpression(ref, p);
	}

	@Override
	public Owner visitMemberRef(MemberRefNode ref, AccessDistributor p) {
		return this.visitor.memberRef(ref, p, this);
	}

	@Override
	public Owner visitAdapterRef(AdapterRefNode ref, AccessDistributor p) {
		return this.visitor.adapterRef(ref, p, this);
	}

	@Override
	public Owner visitDeref(DerefNode ref, AccessDistributor p) {
		return this.visitor.deref(ref, p, this);
	}

	@Override
	public Owner visitEagerRef(EagerRefNode ref, AccessDistributor p) {
		return this.visitor.eagerRef(ref, p, this);
	}

	@Override
	public Owner visitExpression(
			ExpressionNode expression,
			AccessDistributor p) {
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
