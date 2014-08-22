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
package org.o42a.compiler.ip.clause;

import static org.o42a.compiler.ip.Interpreter.CLAUSE_DEF_IP;
import static org.o42a.compiler.ip.access.AccessRules.ACCESS_FROM_PLACEMENT;
import static org.o42a.compiler.ip.clause.ClauseInterpreter.invalidClauseContent;
import static org.o42a.compiler.ip.ref.RefInterpreter.ADAPTER_FIELD_REF_IP;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarableNodeVisitor;
import org.o42a.ast.ref.*;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.ref.Ref;


final class OverriderDeclarableVisitor
		implements DeclarableNodeVisitor<ClauseBuilder, ClauseBuilder> {

	static final OverriderDeclarableVisitor OVERRIDER_DECLARABLE_VISITOR =
			new OverriderDeclarableVisitor();

	private OverriderDeclarableVisitor() {
	}

	@Override
	public ClauseBuilder visitMemberRef(MemberRefNode ref, ClauseBuilder p) {
		if (!checkMemberRef(ref, p)) {
			return p;
		}

		final ClauseBuilder builder =
				p.setOverridden(FIELD_NAME.memberName(ref.getName().getName()));

		return setDeclaredIn(ref, builder);
	}

	@Override
	public ClauseBuilder visitDeclarableAdapter(
			DeclarableAdapterNode adapter,
			ClauseBuilder p) {

		final MemberRefNode memberNode = adapter.getMember();
		final Ref adapterId = memberNode.accept(
				ADAPTER_FIELD_REF_IP.refVisitor(),
				ACCESS_FROM_PLACEMENT.distribute(p.distribute()));

		if (adapterId == null) {
			return null;
		}

		final ClauseBuilder builder =
				p.setOverridden(adapterId(adapterId.toTypeRef()));

		return setDeclaredIn(memberNode, builder);
	}

	@Override
	public ClauseBuilder visitDeclarable(
			DeclarableNode declarable,
			ClauseBuilder p) {
		invalidClauseContent(p.getLogger(), declarable);
		return null;
	}

	private static boolean checkMemberRef(MemberRefNode ref, ClauseBuilder p) {
		if (ref.getName() == null) {
			return false;
		}

		final ExpressionNode owner = ref.getOwner();

		if (owner == null) {
			return true;
		}

		final RefNode ownerRef = owner.toRef();

		if (ownerRef == null) {
			return unexpectedOverriderOwner(ref, p);
		}

		final ScopeRefNode ownerScopeRef = ownerRef.toScopeRef();

		if (ownerScopeRef == null) {
			return unexpectedOverriderOwner(ref, p);
		}
		if (ownerScopeRef.getType() != ScopeType.IMPLIED) {
			return unexpectedOverriderOwner(ref, p);
		}

		return true;
	}

	private static boolean unexpectedOverriderOwner(
			MemberRefNode ref,
			ClauseBuilder p) {
		p.getLogger().error(
				"unexpected_overridder_owner",
				ref.getOwner(),
				"Field owner is not expected here");
		return false;
	}

	private static ClauseBuilder setDeclaredIn(
			MemberRefNode ref,
			ClauseBuilder builder) {

		final RefNode declaredInNode = ref.getDeclaredIn();

		if (declaredInNode == null) {
			return builder;
		}

		final Ref declaredIn = declaredInNode.accept(
				CLAUSE_DEF_IP.refVisitor(),
				ACCESS_FROM_PLACEMENT.distribute(builder.distribute()));

		if (declaredIn == null) {
			return builder;
		}

		return builder.setDeclaredIn(declaredIn.toStaticTypeRef());
	}

}
