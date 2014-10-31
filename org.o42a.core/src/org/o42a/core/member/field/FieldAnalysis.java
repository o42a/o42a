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

import static org.o42a.core.member.field.FieldUsage.*;
import static org.o42a.core.object.type.DerivationUsage.DERIVATION_USAGE;
import static org.o42a.util.fn.Init.init;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.DerivationUsage;
import org.o42a.util.fn.Init;


public class FieldAnalysis implements Uses<FieldUsage> {

	private final MemberField member;
	private final Init<MemberFieldUses> uses =
			init(() -> new MemberFieldUses(getMember()));
	private final Init<Usable<DerivationUsage>> derivationUses =
			init(this::createDerivationUses);

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

	@Override
	public final AllUsages<FieldUsage> allUsages() {
		return ALL_FIELD_USAGES;
	}

	@Override
	public UseFlag selectUse(
			UseCaseInfo useCase,
			UseSelector<FieldUsage> selector) {
		return uses().selectUse(useCase, selector);
	}

	public final User<DerivationUsage> derivation() {
		return derivationUses().toUser();
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

		derivationUses().useBy(
				object.content(),
				DERIVATION_USAGE,
				object.type()::isRuntimeConstructed);
	}

	final MemberFieldUses uses() {
		return this.uses.get();
	}

	private Usable<DerivationUsage> derivationUses() {
		return this.derivationUses.get();
	}

	private Usable<DerivationUsage> createDerivationUses() {

		final Usable<DerivationUsage> derivationUses =
				DerivationUsage.usable("DerivationOf", getMember());
		final ObjectType ownerType = getMember().getMemberOwner().type();

		// If owner derived then member derived too.
		derivationUses.useBy(ownerType.derivation(), DERIVATION_USAGE);

		return derivationUses;
	}

}
