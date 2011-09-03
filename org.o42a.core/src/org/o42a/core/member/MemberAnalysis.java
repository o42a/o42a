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
import org.o42a.core.member.impl.MemberUses;
import org.o42a.util.use.*;


public class MemberAnalysis implements UseInfo {

	private final UseTracker tracker = new UseTracker();
	private final Member member;
	private MemberUses memberUses;
	private MemberUses substanceUses;
	private MemberUses nestedUses;
	private MemberUses runtimeConstructionUses;
	private MemberUses derivationUses;

	MemberAnalysis(Member member) {
		this.member = member;
	}

	public final Member getMember() {
		return this.member;
	}

	public final MemberAnalysis getDeclarationAnalysis() {
		return getMember().getFirstDeclaration().getAnalysis();
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public UseFlag getUseBy(UseCaseInfo useCase) {
		if (!this.tracker.start(useCase.toUseCase())) {
			return this.tracker.getUseFlag();
		}
		if (this.memberUses == null) {
			return this.tracker.done();
		}
		if (!this.tracker.require(this.memberUses)) {
			return this.tracker.getUseFlag();
		}
		if (this.substanceUses != null
				&& this.tracker.useBy(this.substanceUses)) {
			return this.tracker.getUseFlag();
		}
		if (this.nestedUses != null
				&& this.tracker.useBy(this.nestedUses)) {
			return this.tracker.getUseFlag();
		}
		return this.tracker.done();
	}

	public final boolean accessedBy(UseCaseInfo useCase) {
		return this.memberUses.isUsedBy(useCase);
	}

	public final boolean substanceAccessedBy(UseCaseInfo useCase) {
		return this.substanceUses.isUsedBy(useCase);
	}

	public final boolean nestedAccessedBy(UseCaseInfo useCase) {
		return this.nestedUses.isUsedBy(useCase);
	}

	public final UserInfo runtimeConstruction() {

		final MemberUses uses = runtimeConstructionUses();

		if (uses == null) {
			return dummyUser();
		}

		return uses;
	}

	public final UserInfo derivation() {

		final MemberUses uses = derivationUses();

		if (uses == null) {
			return dummyUser();
		}

		return uses;
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
		if (this.memberUses == null) {
			this.memberUses = new MemberUses("MemberUses", getMember());
			if (getMember().isOverride()) {
				getDeclarationAnalysis().useBy(this.memberUses);
			}
		}
		this.memberUses.useBy(user);
	}

	final void useSubstanceBy(UseInfo user) {
		if (this.substanceUses == null) {
			this.substanceUses = new MemberUses("SubstanceUses", getMember());
			if (getMember().isOverride()) {
				getDeclarationAnalysis().useSubstanceBy(this.substanceUses);
			}
		}
		this.substanceUses.useBy(user);
	}

	final void useNestedBy(UseInfo user) {
		if (this.nestedUses == null) {
			this.nestedUses = new MemberUses("NestedUses", getMember());
			if (getMember().isOverride()) {
				getDeclarationAnalysis().useNestedBy(this.nestedUses);
			}
		}
		this.nestedUses.useBy(user);
	}

	private MemberUses runtimeConstructionUses() {
		if (this.runtimeConstructionUses != null) {
			return this.runtimeConstructionUses;
		}

		final Member member = getMember();
		final Field<?> field = member.toField(dummyUser());

		if (field == null) {
			// Member is not field (e.g. it is a clause).
			// No need to track a run-time construction.
			return null;
		}

		this.runtimeConstructionUses =
				new MemberUses("RuntimeConstructionOf", getMember());

		final Obj owner = member.getMemberOwner().toObject();

		if (owner == null) {
			// Owner is not an object (i.e. it is local).
			// Local objects always constructed at run time.
			this.runtimeConstructionUses.useBy(getDeclarationAnalysis());
		} else if (field.isClone()) {
			// Clones are always constructed at run time.
			this.runtimeConstructionUses.useBy(getDeclarationAnalysis());
		} else {
			// Run time construction status derived from owner.
			this.runtimeConstructionUses.useBy(
					owner.type().runtimeConstruction());
		}

		final Member lastDefinition = member.getLastDefinition();

		if (lastDefinition != member) {
			lastDefinition.getAnalysis().runtimeConstructionUses().useBy(
					this.runtimeConstructionUses);
		}

		derivationUses().useBy(this.runtimeConstructionUses);

		return this.runtimeConstructionUses;
	}

	private MemberUses derivationUses() {
		if (this.derivationUses != null) {
			return this.derivationUses;
		}

		final Member member = getMember();

		if (member.toField(dummyUser()) == null) {
			// Member is not field (e.g. it is a clause).
			// No need to track derivation.
			return null;
		}

		this.derivationUses = new MemberUses("DerivationOf", getMember());

		final Obj owner = member.getMemberOwner().toObject();

		if (owner != null) {
			// If owner derived then member derived too.
			this.derivationUses.useBy(owner.type().derivation());
		}

		final Member firstDeclaration = member.getFirstDeclaration();

		if (firstDeclaration != member) {
			firstDeclaration.getAnalysis().derivationUses().useBy(
					this.derivationUses);
		}

		return this.derivationUses;
	}

}
