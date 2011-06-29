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

import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.util.use.*;


public class MemberAnalysis implements UseInfo {

	private final UseTracker tracker = new UseTracker();
	private final Member member;
	private final MemberUses memberUses;
	private final MemberUses substanceUses;
	private final MemberUses nestedUses;

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
		final Obj origin = memberKey.getOrigin().toObject();
		final Member declaration = origin.member(memberKey);

		return declaration.getAnalysis();
	}

	public final boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public UseFlag getUseBy(UseCase useCase) {
		if (!this.tracker.start(useCase)) {
			return this.tracker.getUseFlag();
		}
		if (!this.tracker.require(this.memberUses)) {
			return this.tracker.getUseFlag();
		}
		if (this.tracker.useBy(this.substanceUses)) {
			return this.tracker.getUseFlag();
		}
		if (this.tracker.useBy(this.nestedUses)) {
			return this.tracker.getUseFlag();
		}
		return this.tracker.done();
	}

	public final boolean accessedBy(Generator generator) {
		return this.memberUses.isUsedBy(generator.getUseCase());
	}

	public final boolean substanceAccessedBy(Generator generator) {
		return this.substanceUses.isUsedBy(generator.getUseCase());
	}

	public final boolean nestedAccessedBy(Generator generator) {
		return this.nestedUses.isUsedBy(generator.getUseCase());
	}

	public String reasonNotFound(Generator generator) {

		final Field<?> field = getMember().toField(dummyUser());

		if (field == null) {
			return "not a field";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		if (!accessedBy(generator)) {
			out.append("never accessed");
			comma = true;
		}
		if (!substanceAccessedBy(generator) && !nestedAccessedBy(generator)) {
			if (comma) {
				out.append(", ");
			}
			out.append("neither substance nor nested fields accessed");
		}

		return out.toString();
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return "MemberAnalysis[" + this.member + ']';
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

}
