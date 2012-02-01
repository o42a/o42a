/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.artifact.object.DerivationUsage.RUNTIME_DERIVATION_USAGE;
import static org.o42a.core.artifact.object.DerivationUsage.STATIC_DERIVATION_USAGE;
import static org.o42a.core.member.field.FieldUsage.FIELD_ACCESS;
import static org.o42a.core.member.field.FieldUsage.NESTED_USAGE;
import static org.o42a.core.member.field.FieldUsage.SUBSTANCE_USAGE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.DerivationUsage;
import org.o42a.core.artifact.object.Obj;


public class FieldAnalysis {

	private final MemberField member;
	private MemberFieldUses uses;
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

	public UseFlag selectUse(
			Analyzer analyzer,
			UseSelector<FieldUsage> selector) {
		return uses().selectUse(analyzer, selector);
	}

	public final boolean isUsed(
			Analyzer analyzer,
			UseSelector<FieldUsage> selector) {
		return selectUse(analyzer, selector).isUsed();
	}

	public final User<DerivationUsage> derivation() {
		return derivationUses().toUser();
	}

	public String reasonNotFound(Analyzer analyzer) {

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		if (!uses().isUsed(analyzer, FIELD_ACCESS)) {
			out.append("never accessed");
			comma = true;
		}
		if (!uses().isUsed(analyzer, SUBSTANCE_USAGE)
				&& !uses().isUsed(analyzer, NESTED_USAGE)) {
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

	final void registerArtifact(Artifact<?> artifact) {

		final MemberFieldUses uses = uses();

		uses.useBy(artifact.content().toUser(), SUBSTANCE_USAGE);
		uses.useBy(artifact.fieldUses(), NESTED_USAGE);
	}

	final MemberFieldUses uses() {
		if (this.uses != null) {
			return this.uses;
		}

		this.uses = new MemberFieldUses(getMember());

		if (getMember().isOverride()) {

			final MemberFieldUses declarationUses =
					getDeclarationAnalysis().uses();

			declarationUses.useBy(
					this.uses.usageUser(FIELD_ACCESS),
					FIELD_ACCESS);
			declarationUses.useBy(
					this.uses.usageUser(SUBSTANCE_USAGE),
					SUBSTANCE_USAGE);
			declarationUses.useBy(
					this.uses.usageUser(NESTED_USAGE),
					NESTED_USAGE);
		}

		return this.uses;
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
