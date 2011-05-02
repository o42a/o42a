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

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.util.use.User.dummyUser;

import java.util.Collection;
import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.util.ArrayUtil;
import org.o42a.util.use.UserInfo;


final class ExplicitLocalScope extends LocalScope {

	private final ExplicitMember member;
	private final String name;
	private final HashMap<MemberId, Member> members =
		new HashMap<MemberId, Member>();
	private Clause[] implicitClauses;
	private ImperativeBlock block;
	private boolean allResolved;
	private LocalIR ir;

	ExplicitLocalScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			String name) {
		super(location, owner);
		this.name = name;
		this.member = new ExplicitMember(this, distributor);
	}

	ExplicitLocalScope(
			LocationInfo location,
			Distributor distributor,
			Obj owner,
			ExplicitLocalScope reproducedFrom) {
		super(location, owner);
		this.name = reproducedFrom.getName();
		this.member = new ExplicitMember(this, distributor, reproducedFrom);
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
			if (!clause.isImplicit()) {
				continue;
			}

			implicitClauses = ArrayUtil.append(implicitClauses, clause);
		}

		return this.implicitClauses = implicitClauses;
	}

	@Override
	public Member toMember() {
		return this.member;
	}

	@Override
	public Path member(ScopeInfo user, MemberId memberId, Obj declaredIn) {
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
	public Path findMember(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return member(user, memberId, declaredIn);
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

	private static final class ExplicitMember extends MemberLocal {

		private final LocalScope localScope;
		private final MemberId id;
		private final MemberKey key;

		ExplicitMember(LocalScope localScope, Distributor distributor) {
			super(localScope, distributor, localScope.getOwner());
			this.localScope = localScope;

			final MemberId localId =
				memberName("_local_" + this.localScope.getName());
			final Member member = getContainer().toMember();

			if (member != null
					&& member.getContainer().getScope() == getScope()) {
				this.id = member.getId().append(localId);
			} else {

				assert getContainer().toObject() == this.localScope.getOwner() :
					"Wrong local scope container: " + getContainer();

				this.id = localId;
			}

			this.key = this.id.key(getScope());
		}

		ExplicitMember(
				LocalScope localScope,
				Distributor distributor,
				LocalScope reproducedFrom) {
			super(localScope, distributor, localScope.getOwner());
			this.localScope = localScope;
			this.id =
				reproducedFrom.toMember().getKey().getMemberId()
				.reproduceFrom(reproducedFrom);
			this.key = this.id.key(getScope());
		}

		@Override
		public final MemberId getId() {
			return this.id;
		}

		@Override
		public final MemberKey getKey() {
			return this.key;
		}

		@Override
		public Member getPropagatedFrom() {
			return null;
		}

		@Override
		public LocalScope toLocal(UserInfo user) {
			useBy(user);
			return this.localScope;
		}

		@Override
		protected void useBy(UserInfo user) {
			super.useBy(user);
			this.localScope.newResolver(user);
		}

	}

}
