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
package org.o42a.core.member.field;

import static org.o42a.core.artifact.object.DerivationUsage.RUNTIME_DERIVATION_USAGE;
import static org.o42a.core.artifact.object.DerivationUsage.STATIC_DERIVATION_USAGE;
import static org.o42a.util.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.core.artifact.object.DerivationUsage;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.impl.MemberUses;
import org.o42a.util.use.*;


public class FieldAnalysis implements Uses<SimpleUsage> {

	private final UseTracker tracker = new UseTracker();
	private final MemberField member;
	private MemberUses memberUses;
	private MemberUses substanceUses;
	private MemberUses nestedUses;
	private Usable<DerivationUsage> derivationUses;

	FieldAnalysis(MemberField member) {
		this.member = member;
	}

	public final MemberField getMember() {
		return this.member;
	}

	public final FieldAnalysis getDeclarationAnalysis() {
		return getMember().getFirstDeclaration().getAnalysis();
	}

	@Override
	public final AllUsages<SimpleUsage> allUsages() {
		return ALL_SIMPLE_USAGES;
	}

	@Override
	public UseFlag selectUse(
			UseCaseInfo useCase,
			UseSelector<SimpleUsage> selector) {

		final UseCase uc = useCase.toUseCase();

		if (uc.isSteady()) {
			return uc.usedFlag();
		}
		if (!this.tracker.start(uc)) {
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

	@Override
	public final boolean isUsed(
			UseCaseInfo useCase,
			UseSelector<SimpleUsage> selector) {
		return selectUse(useCase, selector).isUsed();
	}

	public final boolean accessedBy(UseCaseInfo useCase) {
		return this.memberUses.isUsed(useCase, ALL_SIMPLE_USAGES);
	}

	public final boolean substanceAccessedBy(UseCaseInfo useCase) {
		return this.substanceUses.isUsed(useCase, ALL_SIMPLE_USAGES);
	}

	public final boolean nestedAccessedBy(UseCaseInfo useCase) {
		return this.nestedUses.isUsed(useCase, ALL_SIMPLE_USAGES);
	}

	public final User<DerivationUsage> derivation() {
		return derivationUses().toUser();
	}

	public String reasonNotFound(Generator generator) {

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

	final void useBy(Uses<?> user) {
		memberUses().useBy(user);
	}

	final void useSubstanceBy(Uses<?> user) {
		substanceUses().useBy(user);
	}

	final void useNestedBy(Uses<?> user) {
		nestedUses().useBy(user);
	}

	private MemberUses memberUses() {
		if (this.memberUses != null) {
			return this.memberUses;
		}
		this.memberUses = new MemberUses("MemberUses", getMember());
		if (getMember().isOverride()) {
			getDeclarationAnalysis().useBy(this.memberUses);
		}
		return this.memberUses;
	}

	private MemberUses substanceUses() {
		if (this.substanceUses != null) {
			return this.substanceUses;
		}
		this.substanceUses = new MemberUses("SubstanceUses", getMember());
		if (getMember().isOverride()) {
			getDeclarationAnalysis().useSubstanceBy(this.substanceUses);
		}
		return this.substanceUses;
	}

	private MemberUses nestedUses() {
		if (this.nestedUses != null) {
			return this.nestedUses;
		}
		this.nestedUses = new MemberUses("NestedUses", getMember());
		if (getMember().isOverride()) {
			getDeclarationAnalysis().useNestedBy(this.nestedUses);
		}
		return this.nestedUses;
	}

	private Usable<DerivationUsage> derivationUses() {
		if (this.derivationUses != null) {
			return this.derivationUses;
		}

		final MemberField member = getMember();

		this.derivationUses =
				DerivationUsage.usable("DerivationOf", getMember());

		final Obj owner = member.getMemberOwner().getOwner();

		// If owner derived then member derived too.
		this.derivationUses.useBy(
				owner.type().derivation(),
				STATIC_DERIVATION_USAGE);

		final MemberField firstDeclaration = member.getFirstDeclaration();

		if (firstDeclaration != member) {
			firstDeclaration.getAnalysis().derivationUses().useBy(
					this.derivationUses,
					STATIC_DERIVATION_USAGE);
		}

		// Run time construction status derived from owner.
		this.derivationUses.useBy(
				owner.type().rtDerivation(),
				RUNTIME_DERIVATION_USAGE);

		final Obj target =
				member.substance(dummyUser()).toArtifact().materialize();

		if (target.getConstructionMode().isRuntime()) {
			this.derivationUses.useBy(
					target.content(),
					RUNTIME_DERIVATION_USAGE);
		}

		final MemberField lastDefinition = member.getLastDefinition();

		if (lastDefinition != member) {
			lastDefinition.getAnalysis().derivationUses().useBy(
					this.derivationUses.usageUser(RUNTIME_DERIVATION_USAGE),
					RUNTIME_DERIVATION_USAGE);
		}

		return this.derivationUses;
	}

}
