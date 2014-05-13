/*
    Compilation Analysis
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
package org.o42a.analysis.use;

import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.Set;


public abstract class Usable<U extends Usage<U>> implements UserInfo, Uses<U> {

	private static final Object DUMMY = new Object();

	private final AllUsages<U> allUsages;
	private UsedBy<U>[] usedBy;
	private UseCase useCase;

	public Usable(AllUsages<U> allUsages) {
		this.allUsages = allUsages;
	}

	@Override
	public abstract User<U> toUser();

	@Override
	public final AllUsages<U> allUsages() {
		return this.allUsages;
	}

	public final boolean hasUses() {
		return this.usedBy != null;
	}

	public final void useBy(UserInfo user, U usage) {
		user.toUser().use(this, usage);
	}

	public final Set<User<?>> getUsedBy(U usage) {
		if (this.usedBy == null) {
			return emptySet();
		}

		final UsedBy<U> usedBy = this.usedBy[usage.ordinal()];

		if (usedBy == null) {
			return emptySet();
		}

		return usedBy.getUsedBy();
	}

	@Override
	public UseFlag selectUse(UseCaseInfo useCase, UseSelector<U> selector) {

		final UseCase uc = useCase.toUseCase();

		if (uc.isSteady()) {
			return uc.usedFlag();
		}
		this.useCase = uc;
		if (this.usedBy == null) {
			return uc.unusedFlag();
		}

		boolean unknown = false;
		final AllUsages<U> allUsages = allUsages();
		final U[] usages = allUsages.usages();

		for (int i = 0, size = allUsages.size(); i < size; ++i) {

			final UsedBy<U> usedBy = this.usedBy[i];

			if (usedBy == null) {
				continue;
			}

			final U usage = usages[i];

			if (!selector.acceptUsage(usage)) {
				continue;
			}

			final UseFlag useFlag = usedBy.selectUse(useCase);

			if (useFlag.isUsed()) {
				return useFlag;
			}
			unknown |= !useFlag.isKnown();
		}

		return unknown ? uc.checkUseFlag() : uc.unusedFlag();
	}

	public final boolean hasUses(UseSelector<U> selector) {
		if (this.usedBy == null) {
			return false;
		}

		final AllUsages<U> allUsages = allUsages();
		final U[] usages = allUsages.usages();

		for (int i = 0, size = allUsages.size(); i < size; ++i) {

			final UsedBy<U> usedBy = this.usedBy[i];

			if (usedBy == null) {
				continue;
			}

			final U usage = usages[i];

			if (selector.acceptUsage(usage)) {
				return true;
			}
		}

		return false;
	}

	public final User<U> selectiveUser(UseSelector<U> selector) {
		return new SelectiveUser<>(this, selector);
	}

	public final User<U> usageUser(U usage) {
		assert usage != null :
			"Usage not specified";
		return used(usage).user(this, usage);
	}

	@Override
	public String toString() {
		if (this.allUsages == null) {
			return super.toString();
		}
		return getClass().getSimpleName() + '[' + this.allUsages + ']';
	}

	final void useBy(User<?> user, U usage) {
		if (used(usage).addUseBy(user)) {
			if (this.useCase != null) {
				this.useCase.update();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private UsedBy<U> used(U usage) {
		if (this.usedBy == null) {
			this.usedBy = new UsedBy[allUsages().size()];
		}

		final int ordinal = usage.ordinal();
		final UsedBy<U> existing = this.usedBy[ordinal];

		if (existing != null) {
			return existing;
		}

		final UsedBy<U> usedBy = new UsedBy<>();

		this.usedBy[ordinal] = usedBy;

		return usedBy;
	}

	private static final class UsedBy<U extends Usage<U>> extends UseTracker {

		private final HashMap<User<?>, Object> usedBy = new HashMap<>();
		private SelectiveUser<U> user;

		public final Set<User<?>> getUsedBy() {
			return this.usedBy.keySet();
		}

		public UseFlag selectUse(UseCaseInfo useCase) {
			if (!start(useCase.toUseCase())) {
				return getUseFlag();
			}
			for (User<?> user : getUsedBy()) {
				if (useBy(user)) {
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

		final boolean addUseBy(User<?> user) {
			return this.usedBy.put(user, DUMMY) == null;
		}

		final User<U> user(Usable<U> usable, U usage) {
			if (this.user != null) {
				return this.user;
			}
			return this.user = new SelectiveUser<>(usable, usage);
		}

	}

}
