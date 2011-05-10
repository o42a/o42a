/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.member;

import org.o42a.core.artifact.object.Obj;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.UseInfo;


public class MemberAnalysis implements UseInfo {

	private final Member member;
	private final MemberUses memberUses;
	private final MemberUses substanceUses;
	private final MemberUses nestedUses;
	private Status status = Status.NOT_ANALYSED;

	MemberAnalysis(Member member) {
		this.member = member;
		this.memberUses = new MemberUses("MemberUses", member);
		this.substanceUses = new MemberUses("SubstanceUses", member);
		this.nestedUses = new MemberUses("NestedUses", member);
		if (member.isOverride()) {
			// Member declaration should be used when overridden member used.
			getDeclarationAnalysis().useBy(this.memberUses);
			getDeclarationAnalysis().useSubstanceBy(this.substanceUses);
			getDeclarationAnalysis().useNestedBy(this.nestedUses);
		}
	}

	public final Member getMember() {
		return this.member;
	}

	public final MemberAnalysis getDeclarationAnalysis() {
		if (!getMember().isOverride()) {
			return this;
		}

		final MemberKey memberKey = getMember().getKey();
		final Obj origin = memberKey.getOrigin().getContainer().toObject();
		final Member declaration = origin.member(memberKey);

		return declaration.getAnalysis();
	}

	@Override
	public boolean isUsedBy(UseCase useCase) {
		if (!accessedBy(useCase)) {
			return false;
		}
		if (substanceAccessedBy(useCase)) {
			return true;
		}
		if (nestedAccessedBy(useCase)) {
			return true;
		}
		return false;
	}

	public final boolean accessedBy(UseCase useCase) {
		return this.memberUses.isUsedBy(useCase);
	}

	public final boolean substanceAccessedBy(UseCase useCase) {
		return this.substanceUses.isUsedBy(useCase);
	}

	public final boolean nestedAccessedBy(UseCase useCase) {
		this.status = Status.ANALYSING;
		try {
			return this.nestedUses.isUsedBy(useCase);
		} finally {
			this.status = Status.ANALYSED;
		}
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return "MemberAnalysis[" + this.member + ']';
	}

	final Status getStatus() {
		return this.status;
	}

	final void useBy(UseInfo user) {
		this.memberUses.useBy(user);
	}

	final void useSubstanceBy(UseInfo user) {
		this.substanceUses.useBy(user);
	}

	final void useNestedBy(UseInfo user) {
		this.nestedUses.useBy(user);
	}

	enum Status {

		NOT_ANALYSED,
		ANALYSING,
		ANALYSED

	}

}
