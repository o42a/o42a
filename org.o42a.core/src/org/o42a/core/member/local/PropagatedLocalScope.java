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

import java.util.Collection;

import org.o42a.codegen.Generator;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.util.use.UserInfo;


final class PropagatedLocalScope extends LocalScope {

	private final ExplicitLocalScope explicit;
	private final PropagatedMember member;

	PropagatedLocalScope(LocalScope overridden, Obj owner) {
		super(overridden, owner);
		this.explicit = overridden.explicit();
		this.member = new PropagatedMember(this, overridden);
	}

	@Override
	public Obj getSource() {
		return this.explicit.getSource();
	}

	@Override
	public String getName() {
		return explicit().getName();
	}

	@Override
	public ImperativeBlock getBlock() {
		return this.explicit.getBlock();
	}

	@Override
	public Collection<Member> getMembers() {
		return this.explicit.getMembers();
	}

	@Override
	public Clause[] getImplicitClauses() {
		return this.explicit.getImplicitClauses();
	}

	@Override
	public Member toMember() {
		return this.member;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return this.explicit.member(memberKey);
	}

	@Override
	public Path member(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return this.explicit.member(user, memberId, declaredIn);
	}

	@Override
	public Clause clause(MemberId memberId, Obj declaredIn) {
		return this.explicit.clause(memberId, declaredIn);
	}

	@Override
	public Path findMember(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return this.explicit.findMember(user, memberId, declaredIn);
	}

	@Override
	public LocalIR ir(Generator generator) {
		return explicit().ir(generator);
	}

	@Override
	public String toString() {
		return this.member.toString();
	}

	@Override
	final ExplicitLocalScope explicit() {
		return this.explicit;
	}

	@Override
	boolean addMember(Member member) {
		throw new UnsupportedOperationException(
				"Can not register field in propagated local scope " + this);
	}

	private static final class PropagatedMember extends MemberLocal {

		private final PropagatedLocalScope localScope;
		private final LocalScope overridden;

		PropagatedMember(
				PropagatedLocalScope localScope,
				LocalScope overridden) {
			super(
					localScope,
					localScope.getOwner().distribute(),
					localScope.getOwner());
			this.localScope = localScope;
			this.overridden = overridden;
		}

		@Override
		public MemberId getId() {
			return this.localScope.explicit.toMember().getId();
		}

		@Override
		public MemberKey getKey() {
			return this.localScope.explicit.toMember().getKey();
		}

		@Override
		public Member getPropagatedFrom() {
			return this.overridden.toMember();
		}

		@Override
		public LocalScope toLocal(UserInfo user) {
			this.localScope.toUser().useBy(user);
			return this.localScope;
		}

	}

}
