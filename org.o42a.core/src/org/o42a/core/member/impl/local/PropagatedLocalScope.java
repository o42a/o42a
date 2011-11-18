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
package org.o42a.core.member.impl.local;

import java.util.Collection;

import org.o42a.codegen.Generator;
import org.o42a.core.PlaceInfo;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.sentence.ImperativeBlock;


public final class PropagatedLocalScope extends LocalScope {

	private final ExplicitLocalScope explicit;

	public PropagatedLocalScope(
			PropagatedMemberLocal member,
			LocalScope propagatedFrom) {
		super(member);
		this.explicit = explicitLocal(propagatedFrom);
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
	public MemberClause[] getImplicitClauses() {
		return this.explicit.getImplicitClauses();
	}

	@Override
	public Member member(MemberKey memberKey) {
		return this.explicit.member(memberKey);
	}

	@Override
	public Path member(PlaceInfo user, Accessor accessor, MemberId memberId, Obj declaredIn) {
		return this.explicit.member(user, accessor, memberId, declaredIn);
	}

	@Override
	public boolean hasSubClauses() {
		return this.explicit.hasSubClauses();
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {
		return this.explicit.clause(memberId, declaredIn);
	}

	@Override
	public Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		return this.explicit.findMember(user, accessor, memberId, declaredIn);
	}

	@Override
	public void resolveAll() {
		this.explicit.resolveAll();
	}

	@Override
	public LocalIR ir(Generator generator) {
		return explicit().ir(generator);
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
