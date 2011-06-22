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

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.Map;


public abstract class Usable<U> implements UserInfo, UseInfo {

	public static Usable<?> simpleUsable(String name) {
		return new SimpleUsable<Void>(name, null);
	}

	public static <U> Usable<U> simpleUsable(String name, U used) {
		return new SimpleUsable<U>(name, used);
	}

	private final UseTracker tracker = new UseTracker();
	private HashMap<User, U> usedBy;

	public final U useBy(UserInfo user) {
		return user.toUser().use(this);
	}

	public final Map<User, U> getUsedBy() {
		if (this.usedBy == null) {
			return emptyMap();
		}
		return this.usedBy;
	}

	@Override
	public UseFlag getUseBy(UseCase useCase) {
		if (!this.tracker.start(useCase)) {
			return this.tracker.getUseFlag();
		}
		if (this.usedBy == null) {
			return this.tracker.done();
		}

		for (User user : this.usedBy.keySet()) {
			if (this.tracker.useBy(user)) {
				return this.tracker.getUseFlag();
			}
		}

		return this.tracker.done();
	}

	public final boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public String toString() {
		if (this.usedBy == null) {
			return getClass().getSimpleName() + "[]";
		}
		return getClass().getSimpleName() + this.usedBy.values().toString();
	}

	protected abstract U createUsed(User user);

	final U useBy(User user) {
		if (this.usedBy == null) {
			this.usedBy = new HashMap<User, U>();
		} else {

			final U found = this.usedBy.get(user);

			if (found != null) {
				return found;
			}
		}

		final U use = createUsed(user);

		this.usedBy.put(user, use);

		return use;
	}

}
