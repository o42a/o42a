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

import java.util.HashSet;
import java.util.Set;


public abstract class Usable implements UserInfo, UseInfo {

	public static Usable simpleUsable(String name) {
		return new SimpleUsable(name, null);
	}

	public static Usable simpleUsable(String name, Object used) {
		return new SimpleUsable(name, used);
	}

	private final UseTracker tracker = new UseTracker();
	private HashSet<User> usedBy;

	public final void useBy(UserInfo user) {
		user.toUser().use(this);
	}

	public final Set<User> getUsedBy() {
		if (this.usedBy == null) {
			return emptySet();
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

		for (User user : this.usedBy) {
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
		return getClass().getSimpleName() + this.usedBy.toString();
	}

	final void useBy(User user) {
		if (this.usedBy == null) {
			this.usedBy = new HashSet<User>();
		}
		this.usedBy.add(user);
	}

}
