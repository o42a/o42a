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

import java.util.HashMap;
import java.util.Map;


public abstract class Useable<U> extends User {

	private HashMap<User, Use<U>> usedBy;

	public final Use<U> useBy(UserInfo user) {
		return user.toUser().use(this);
	}

	public final Map<User, Use<U>> usedBy() {
		return this.usedBy;
	}

	@Override
	public String toString() {
		if (this.usedBy == null) {
			return getClass().getSimpleName() + "[]";
		}
		return getClass().getSimpleName() + this.usedBy.values().toString();
	}

	protected abstract U createUsed(User user);

	@Override
	protected boolean determineUseBy(UseCase useCase) {
		if (this.usedBy == null) {
			return false;
		}
		for (User user : this.usedBy.keySet()) {
			if (user.usedBy(useCase)) {
				return true;
			}
		}
		return false;
	}

	final Use<U> useBy(User user) {
		if (this.usedBy == null) {
			this.usedBy = new HashMap<User, Use<U>>(1);
		} else {

			final Use<U> found = this.usedBy.get(user);

			if (found != null) {
				return found;
			}
		}

		final Use<U> use = new Use<U>(user, createUsed(user), this);

		this.usedBy.put(user, use);

		return use;
	}

	final void released(Use<U> use) {
		this.usedBy.remove(use.getUser());
	}

}
