/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.core.member.field.FieldUsage.FIELD_ACCESS;
import static org.o42a.core.member.field.FieldUsage.NESTED_USAGE;
import static org.o42a.core.member.field.FieldUsage.SUBSTANCE_USAGE;
import static org.o42a.core.object.type.DerivationUsage.RUNTIME_DERIVATION_USAGE;
import static org.o42a.core.object.type.DerivationUsage.STATIC_DERIVATION_USAGE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.DerivationUsage;


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

	public UserInfo fieldAccess() {
		return uses().usageUser(FIELD_ACCESS);
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

	public final User<DerivationUsage> rtDerivation() {
		return derivationUses().usageUser(RUNTIME_DERIVATION_USAGE);
	}

	public final User<DerivationUsage> staticDerivation() {
		return derivationUses().usageUser(STATIC_DERIVATION_USAGE);
	}

	public String reasonNotFound(Analyzer analyzer) {

		final StringBuilder out = new StringBuilder();
		final boolean substanceUsed = uses().isUsed(analyzer, SUBSTANCE_USAGE);
		final boolean nestedUsed = uses().isUsed(analyzer, NESTED_USAGE);

		if (!uses().isUsed(analyzer, FIELD_ACCESS)) {
			if (!substanceUsed && !nestedUsed) {
				out.append("never used");
			} else {
				out.append("never accessed, but ");
				if (substanceUsed) {
					if (nestedUsed) {
						out.append("content");
					} else {
						out.append("substance");
					}
				} else {
					out.append("nested fields");
				}
				out.append(" used");
			}
		} else if (!substanceUsed && !nestedUsed) {
			out.append("accessed, but content never used");
		}

		return out.toString();
	}

	@Override
	public String toString() {
		if (this.member == null) {
			return super.toString();
		}
		return "FieldAnalysis[" + this.member + ']';
	}

	final void registerObject(Obj object) {

		final MemberFieldUses uses = uses();

		uses.useBy(object.content().toUser(), SUBSTANCE_USAGE);
		uses.useBy(object.fieldUses(), NESTED_USAGE);

		if (object.type().isRuntimeConstructed()) {
			derivationUses().useBy(object.content(), RUNTIME_DERIVATION_USAGE);
		}
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

		final ObjectType ownerType = member.getMemberOwner().type();

		// If owner derived then member derived too.
		this.derivationUses.useBy(
				ownerType.staticDerivation(),
				STATIC_DERIVATION_USAGE);
		this.derivationUses.useBy(
				ownerType.rtDerivation(),
				RUNTIME_DERIVATION_USAGE);

		final MemberField firstDeclaration = member.getFirstDeclaration();

		if (firstDeclaration != member) {
			firstDeclaration.getAnalysis().derivationUses().useBy(
					this.derivationUses,
					STATIC_DERIVATION_USAGE);
			if (!member.isUpdated()) {

				final MemberField lastDefinition = member.getLastDefinition();

				if (lastDefinition != member) {
					lastDefinition.getAnalysis().derivationUses().useBy(
							rtDerivation(),
							RUNTIME_DERIVATION_USAGE);
				}
			}
		}

		return this.derivationUses;
	}

}
