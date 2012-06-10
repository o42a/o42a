/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.member.clause;

import static org.o42a.core.AbstractContainer.parentContainer;

import java.util.Collection;

import org.o42a.core.*;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.util.ArrayUtil;


public abstract class GroupClause extends Clause implements Container {

	private MemberClause[] subClauses;

	public GroupClause(MemberClause member) {
		super(member);
		assert member.getDeclaration().getKind() == ClauseKind.GROUP :
			"Attempt to create group instead of plain clause";
	}

	@Override
	public Scope getScope() {
		return toMember().getScope();
	}

	@Override
	public final Container getContainer() {
		return this;
	}

	@Override
	public final Container getParentContainer() {
		return parentContainer(this);
	}

	@Override
	public final Container getEnclosingContainer() {
		return toMember().getContainer();
	}

	@Override
	public Scope getEnclosingScope() {
		return toMember().getScope();
	}

	@Override
	public final boolean requiresInstance() {
		return true;
	}

	public abstract boolean isImperative();

	public abstract LocalScope getLocalScope();

	@Override
	public final PlainClause toPlainClause() {
		return null;
	}

	@Override
	public final GroupClause toGroupClause() {
		return this;
	}

	@Override
	public MemberClause[] getSubClauses() {
		if (this.subClauses != null) {
			return this.subClauses;
		}
		if (getLocalScope() != null) {
			return this.subClauses = new MemberClause[0];
		}

		MemberClause[] subClauses = new MemberClause[0];

		final MemberKey key = getKey();
		final Collection<? extends Member> members =
				getEnclosingScope().getContainer().getMembers();

		for (Member member : members) {

			final MemberClause clause = member.toClause();

			if (clause == null) {
				continue;
			}
			if (!key.equals(member.getKey().getEnclosingKey())) {
				continue;
			}

			subClauses = ArrayUtil.append(subClauses, clause);
		}

		return this.subClauses = subClauses;
	}

	@Override
	public final Obj toObject() {
		return getEnclosingContainer().toObject();
	}

	@Override
	public final Clause toClause() {
		return this;
	}

	@Override
	public final LocalScope toLocal() {
		return getEnclosingContainer().toLocal();
	}

	@Override
	public Namespace toNamespace() {
		return getEnclosingContainer().toNamespace();
	}

	@Override
	public Member member(MemberKey memberKey) {
		return getEnclosingContainer().member(memberKey);
	}

	@Override
	public Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		if (getLocalScope() != null) {
			return null;
		}

		if (memberId.getEnclosingId() == null) {
			return getEnclosingContainer().member(
					user,
					accessor,
					getDeclaration().getMemberId().append(memberId),
					declaredIn);
		}

		return getEnclosingContainer().member(
				user,
				accessor,
				memberId,
				declaredIn);
	}

	@Override
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		if (getLocalScope() != null) {
			return null;
		}

		if (memberId.getEnclosingId() == null) {

			final Path foundInGroup = getEnclosingContainer().findMember(
					user,
					accessor,
					getDeclaration().getMemberId().append(memberId),
					declaredIn);

			if (foundInGroup != null) {
				return foundInGroup;
			}
		}

		return getEnclosingContainer().findMember(
				user,
				accessor,
				memberId,
				declaredIn);
	}

	@Override
	protected void fullyResolve() {
	}

	@Override
	protected Path buildPathInObject() {

		final Path pathInObject = super.buildPathInObject();
		final LocalScope localScope = getLocalScope();

		if (localScope == null) {
			return pathInObject;
		}

		final Member member = localScope.toMember();
		final Scope enclosingScope = getEnclosingScope();
		final Clause enclosingClause = enclosingScope.getContainer().toClause();

		if (enclosingClause == null) {
			assert enclosingScope.toObject() != null :
				this + " is not inside of object";
			return member.getKey().toPath();
		}

		return enclosingClause.pathInObject().append(member.getKey());
	}

	protected MemberClause groupClause(MemberId memberId, Obj declaredIn) {

		final ClauseContainer clauseContainer;
		final Container container = getEnclosingScope().getContainer();
		final Clause clause = container.toClause();

		if (clause != null) {
			clauseContainer = clause.getClauseContainer();
		} else {

			final Obj object = container.toObject();

			assert object != null :
				"Clause container expected: " + container;

			clauseContainer = object;
		}

		return clauseContainer.clause(
				toMember().getId().append(memberId),
				declaredIn);
	}

}
