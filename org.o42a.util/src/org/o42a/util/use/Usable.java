/*
    Utilities
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
package org.o42a.util.use;

import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.Set;


public abstract class Usable<U extends Usage<U>> implements UserInfo, Uses<U> {

	private static final Object DUMMY = new Object();

	private final AllUsages<U> allUsages;
	private final UsedBy<U>[] usedBy;

	@SuppressWarnings("unchecked")
	public Usable(AllUsages<U> allUsages) {
		this.allUsages = allUsages;
		this.usedBy = new UsedBy[allUsages.size()];
	}

	@Override
	public abstract User<U> toUser();

	@Override
	public final AllUsages<U> allUsages() {
		return this.allUsages;
	}

	public final void useBy(UserInfo user, U usage) {
		user.toUser().use(this, usage);
	}

	public final Set<User<?>> getUsedBy(U usage) {

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
			unknown |= useFlag.isKnown();
		}

		return unknown ? uc.checkUseFlag() : uc.unusedFlag();
	}

	@Override
	public final boolean isUsed(
			UseCaseInfo useCase,
			UseSelector<U> selector) {
		return selectUse(useCase, selector).isUsed();
	}

	public final User<U> selectiveUser(UseSelector<U> selector) {
		return new SelectiveUser<U>(this, selector);
	}

	public final User<U> usageUser(U usage) {
		assert usage != null :
			"Usage not specified";
		return used(usage).user(this, usage);
	}

	@Override
	public String toString() {
		if (this.usedBy == null) {
			return getClass().getSimpleName() + "[]";
		}
		return getClass().getSimpleName() + this.usedBy.toString();
	}

	final void useBy(User<?> user, U usage) {
		used(usage).addUseBy(user);
	}

	private UsedBy<U> used(U usage) {

		final int ordinal = usage.ordinal();
		final UsedBy<U> existing = this.usedBy[ordinal];

		if (existing != null) {
			return existing;
		}

		final UsedBy<U> usedBy = new UsedBy<U>();

		this.usedBy[ordinal] = usedBy;

		return usedBy;
	}

	private static final class UsedBy<U extends Usage<U>> extends UseTracker {

		private final HashMap<User<?>, Object> usedBy =
				new HashMap<User<?>, Object>();
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
			return done();
		}

		@Override
		public String toString() {
			if (this.usedBy == null) {
				return "{}";
			}
			return this.usedBy.keySet().toString();
		}

		final void addUseBy(User<?> user) {
			this.usedBy.put(user, Usable.DUMMY);
		}

		final User<U> user(Usable<U> usable, U usage) {
			if (this.user != null) {
				return this.user;
			}
			return this.user = new SelectiveUser<U>(usable, usage);
		}

	}

}
