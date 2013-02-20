/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.member.local.impl;

import java.util.Collection;
import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.Name;


public final class ExplicitLocalScope extends LocalScope {

	private final Name name;
	private final HashMap<MemberId, Member> members = new HashMap<>();
	private MemberClause[] implicitClauses;
	private ImperativeBlock block;
	private LocalIR ir;
	private byte hasSubClauses;

	public ExplicitLocalScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			Name name) {
		super(new ExplicitMemberLocal(
				location,
				distributor,
				owner.toMemberOwner()));
		this.name = name;
		((ExplicitMemberLocal) toMember()).init(this);
	}

	public ExplicitLocalScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			ExplicitLocalScope reproducedFrom) {
		super(new ExplicitMemberLocal(
				location,
				distributor,
				owner.toMemberOwner()));
		this.name = reproducedFrom.getName();
		((ExplicitMemberLocal) toMember()).initReproduced(this, reproducedFrom);
	}

	@Override
	public final LocalScope getPropagatedFrom() {
		return null;
	}

	@Override
	public final Obj getSource() {
		return getOwner();
	}

	@Override
	public final Name getName() {
		return this.name;
	}

	@Override
	public ImperativeBlock getBlock() {
		return this.block;
	}

	@Override
	public Collection<Member> getMembers() {
		return this.members.values();
	}

	@Override
	public MemberClause[] getImplicitClauses() {
		if (this.implicitClauses != null) {
			return this.implicitClauses;
		}

		MemberClause[] implicitClauses = new MemberClause[0];

		for (Member member : getMembers()) {

			final MemberClause clause = member.toClause();

			if (clause == null) {
				continue;
			}
			this.hasSubClauses = 1;
			if (!clause.isImplicit()) {
				continue;
			}

			implicitClauses = ArrayUtil.append(implicitClauses, clause);
		}
		if (this.hasSubClauses == 0) {
			this.hasSubClauses = -1;
		}

		return this.implicitClauses = implicitClauses;
	}

	@Override
	public Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		if (declaredIn != null) {
			return null;
		}

		final Member member = this.members.get(memberId);

		if (member == null) {
			// Member not found.
			return null;
		}
		if (member.toClause() != null) {
			// Clauses are available from outside the local scope.
			return member.getMemberKey().toPath();
		}

		final LocalPlace userPlace = placeOf(user);

		if (userPlace == null) {
			// User is not inside of this scope.
			return null;
		}

		final ScopePlace fieldPlace = member.getPlace();

		if (!fieldPlace.getPlace().visibleBy(userPlace.getPlace())) {
			// User appears before field declaration.
			return null;
		}

		return member.getMemberKey().toPath();
	}

	@Override
	public boolean hasSubClauses() {
		getImplicitClauses();
		return this.hasSubClauses > 0;
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {

		if (declaredIn != null) {
			return null;
		}

		final Member member = this.members.get(memberId);

		if (member == null) {
			// Member not found.
			return null;
		}

		return member.toClause();
	}

	@Override
	public Member member(MemberKey memberKey) {
		if (memberKey.getOrigin() != this) {
			return null;
		}
		return this.members.get(memberKey.getMemberId());
	}

	@Override
	public LocalIR ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = new LocalIR(generator, this);
		}
		return this.ir;
	}

	@Override
	protected void setBlock(ImperativeBlock block) {
		assert block.isTopLevel() :
			block + " is not a top-level imperative block";
		this.block = block;
	}

	@Override
	protected ExplicitLocalScope explicit() {
		return this;
	}

	@Override
	protected boolean addMember(Member member) {

		final MemberId memberId = member.getMemberId();
		final Member old = this.members.put(memberId, member);

		if (old == null) {
			return true;
		}

		this.members.put(memberId, old);

		final MemberField field = old.toField();

		if (field != null) {
			getLogger().ambiguousMember(
					member.getLocation(),
					field.getDisplayName());
		} else {
			getLogger().ambiguousClause(
					member.getLocation(),
					member.toClause().getDisplayName());
		}

		return false;
	}

}
