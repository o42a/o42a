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

import java.util.HashMap;
import java.util.Set;

import org.o42a.analysis.use.*;


final class MemberFieldUses implements UserInfo, Uses<FieldUsage> {

	private static final Object DUMMY = new Object();

	private final MemberField field;
	private final FieldUser user;
	private final UsedBy[] usedBy;
	private UseCase useCase;

	MemberFieldUses(MemberField field) {
		this.field = field;
		this.user = new FieldUser();
		this.usedBy = new UsedBy[ALL_FIELD_USAGES.size()];
	}

	@Override
	public final User<FieldUsage> toUser() {
		return this.user;
	}

	@Override
	public final AllUsages<FieldUsage> allUsages() {
		return ALL_FIELD_USAGES;
	}

	@Override
	public UseFlag selectUse(
			UseCaseInfo useCase,
			UseSelector<FieldUsage> selector) {

		final UseCase uc = useCase.toUseCase();

		if (uc.isSteady()) {
			return uc.usedFlag();
		}
		this.useCase = uc;

		boolean unknown = false;

		// Field access is required.
		if (selector.acceptUsage(FIELD_ACCESS)) {

			final UseFlag fieldAccess = used(FIELD_ACCESS).selectUse(useCase);

			if (!fieldAccess.isUsed()) {
				if (fieldAccess.isKnown()) {
					return fieldAccess;
				}
				unknown = true;
			}
		}

		// At least one of the other usages is required.
		final AllUsages<FieldUsage> allUsages = allUsages();
		final FieldUsage[] usages = allUsages.usages();
		boolean accepted = false;

		for (int i = 1, size = allUsages.size(); i < size; ++i) {

			final FieldUsage usage = usages[i];

			if (!selector.acceptUsage(usage)) {
				continue;
			}
			accepted = true;

			final UsedBy usedBy = this.usedBy[i];

			if (usedBy == null) {
				continue;
			}


			final UseFlag useFlag = usedBy.selectUse(useCase);

			if (useFlag.isUsed()) {
				return useFlag;
			}
			unknown |= !useFlag.isKnown();
		}

		if (unknown) {
			return uc.checkUseFlag();
		}
		if (accepted) {
			return uc.unusedFlag();
		}

		return uc.usedFlag();
	}

	public final User<FieldUsage> usageUser(FieldUsage usage) {
		assert usage != null :
			"Usage not specified";
		return used(usage).user(this, usage);
	}

	@Override
	public String toString() {
		if (this.field == null) {
			return super.toString();
		}
		return "FieldUses[" + this.field + ']';
	}

	final void useByEach(MemberFieldUses uses) {
		useBy(uses.usageUser(FIELD_ACCESS), FIELD_ACCESS);
		useBy(uses.usageUser(SUBSTANCE_USAGE), SUBSTANCE_USAGE);
		useBy(uses.usageUser(NESTED_USAGE), NESTED_USAGE);
	}

	final void useBy(Uses<?> user, FieldUsage usage) {
		if (used(usage).addUseBy(user)) {
			if (this.useCase != null) {
				this.useCase.update();
			}
		}
	}

	private UsedBy used(FieldUsage usage) {

		final int ordinal = usage.ordinal();
		final UsedBy existing = this.usedBy[ordinal];

		if (existing != null) {
			return existing;
		}

		final UsedBy usedBy = new UsedBy();

		this.usedBy[ordinal] = usedBy;

		return usedBy;
	}

	private final class FieldUser extends AbstractUser<FieldUsage> {

		FieldUser() {
			super(ALL_FIELD_USAGES);
		}

		@Override
		public UseFlag selectUse(
				UseCaseInfo useCase,
				UseSelector<FieldUsage> selector) {
			return MemberFieldUses.this.selectUse(useCase, selector);
		}

		@Override
		public String toString() {
			return MemberFieldUses.this.toString();
		}

	}

	private static final class UsedBy extends UseTracker {

		private final HashMap<Uses<?>, Object> usedBy = new HashMap<>();
		private SelectiveUser<FieldUsage> user;

		public final Set<Uses<?>> getUsedBy() {
			return this.usedBy.keySet();
		}

		public UseFlag selectUse(UseCaseInfo useCase) {
			if (!start(useCase.toUseCase())) {
				return getUseFlag();
			}
			for (Uses<?> uses : getUsedBy()) {
				if (useBy(uses)) {
					return getUseFlag();
				}
			}
			return unused();
		}

		@Override
		public String toString() {
			if (this.usedBy == null) {
				return "{}";
			}
			return this.usedBy.keySet().toString();
		}

		final boolean addUseBy(Uses<?> uses) {
			return this.usedBy.put(uses, DUMMY) == null;
		}

		final User<FieldUsage> user(MemberFieldUses uses, FieldUsage usage) {
			if (this.user != null) {
				return this.user;
			}
			return this.user = new SelectiveUser<>(uses, usage);
		}

	}

}
