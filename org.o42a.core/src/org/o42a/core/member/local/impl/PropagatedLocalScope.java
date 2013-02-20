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
import org.o42a.core.PlaceInfo;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.util.string.Name;


public final class PropagatedLocalScope extends LocalScope {

	private final ExplicitLocalScope explicit;
	private HashMap<MemberKey, Member> members;
	private boolean allMembersPropagated;
	private LocalScope propagatedFrom;

	public PropagatedLocalScope(
			PropagatedMemberLocal member,
			LocalScope propagatedFrom) {
		super(member);
		this.propagatedFrom = propagatedFrom;
		this.explicit = explicitLocal(propagatedFrom);
	}

	@Override
	public final LocalScope getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	public Obj getSource() {
		return explicit().getSource();
	}

	@Override
	public Name getName() {
		return explicit().getName();
	}

	@Override
	public ImperativeBlock getBlock() {
		return explicit().getBlock();
	}

	@Override
	public Collection<Member> getMembers() {
		if (this.allMembersPropagated) {
			return this.members.values();
		}

		final Collection<Member> explicitMembers = explicit().getMembers();

		if (this.members == null) {
			this.members = new HashMap<>(explicitMembers.size());
		}

		for (Member member : explicitMembers) {
			member(member.getMemberKey());
		}
		this.allMembersPropagated = true;

		return explicitMembers;
	}

	@Override
	public MemberClause[] getImplicitClauses() {
		return explicit().getImplicitClauses();
	}

	@Override
	public Member member(MemberKey memberKey) {
		if (this.members != null) {

			final Member found = this.members.get(memberKey);

			if (found != null) {
				return found;
			}
		} else {
			this.members = new HashMap<>();
		}

		final Member sample = getPropagatedFrom().member(memberKey);

		if (sample == null) {
			return null;
		}

		final Member propagated = sample.propagateTo(toOwner());

		this.members.put(sample.getMemberKey(), propagated);

		return propagated;
	}

	@Override
	public Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return explicit().member(user, accessor, memberId, declaredIn);
	}

	@Override
	public boolean hasSubClauses() {
		return explicit().hasSubClauses();
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {
		return explicit().clause(memberId, declaredIn);
	}

	@Override
	public LocalIR ir(Generator generator) {
		return explicit().ir(generator);
	}

	@Override
	protected void setBlock(ImperativeBlock block) {
		assert block.isTopLevel() :
			block + " is not a top-level imperative block";
	}

	@Override
	protected final ExplicitLocalScope explicit() {
		return this.explicit;
	}

	@Override
	protected boolean addMember(Member member) {
		throw new UnsupportedOperationException(
				"Can not register field in propagated local scope " + this);
	}

}
