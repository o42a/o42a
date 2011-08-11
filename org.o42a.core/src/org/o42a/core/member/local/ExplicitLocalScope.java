/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member.local;

import static org.o42a.util.use.User.dummyUser;

import java.util.Collection;
import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.util.ArrayUtil;


final class ExplicitLocalScope extends LocalScope {

	private final ExplicitMemberLocal member;
	private final String name;
	private final HashMap<MemberId, Member> members =
			new HashMap<MemberId, Member>();
	private Clause[] implicitClauses;
	private ImperativeBlock block;
	private byte hasSubClauses;
	private boolean allResolved;
	private LocalIR ir;

	ExplicitLocalScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			String name) {
		super(location, owner);
		this.name = name;
		this.member = new ExplicitMemberLocal(this, distributor);
	}

	ExplicitLocalScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			ExplicitLocalScope reproducedFrom) {
		super(location, owner);
		this.name = reproducedFrom.getName();
		this.member =
				new ExplicitMemberLocal(this, distributor, reproducedFrom);
	}

	@Override
	public final Obj getSource() {
		return getOwner();
	}

	@Override
	public final String getName() {
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
	public Clause[] getImplicitClauses() {
		if (this.implicitClauses != null) {
			return this.implicitClauses;
		}

		Clause[] implicitClauses = new Clause[0];

		for (Member member : getMembers()) {

			final Clause clause = member.toClause();

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
	public MemberLocal toMember() {
		return this.member;
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
			return member.getKey().toPath();
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

		return member.getKey().toPath();
	}

	@Override
	public boolean hasSubClauses() {
		getImplicitClauses();
		return this.hasSubClauses > 0;
	}

	@Override
	public Clause clause(MemberId memberId, Obj declaredIn) {

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
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return member(user, accessor, memberId, declaredIn);
	}

	@Override
	public Member member(MemberKey memberKey) {
		if (memberKey.getOrigin() != this) {
			return null;
		}
		return this.members.get(memberKey.getMemberId());
	}

	@Override
	public void resolveAll() {
		if (this.allResolved) {
			return;
		}
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			for (Member member : getMembers()) {
				member.resolveAll();
			}
		} finally {
			getContext().fullResolution().end();
		}
	}

	@Override
	public LocalIR ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = new LocalIR(generator, this);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		return this.member.toString();
	}

	@Override
	protected void setBlock(ImperativeBlock block) {
		assert block.isTopLevel() :
			block + " is not a top-level imperative block";
		this.block = block;
	}

	@Override
	ExplicitLocalScope explicit() {
		return this;
	}

	@Override
	boolean addMember(Member member) {

		final MemberId memberId = member.getId();
		final Member old = this.members.put(memberId, member);

		if (old == null) {
			return true;
		}

		this.members.put(memberId, old);

		final Field<?> field = old.toField(dummyUser());

		if (field != null) {
			getLogger().ambiguousMember(member, field.getDisplayName());
		} else {
			getLogger().ambiguousClause(
					member,
					member.toClause().getDisplayName());
		}

		return false;
	}

}
