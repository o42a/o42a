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


public abstract class Usable<U> extends User {

	public static <U> Usable<U> simpleUsable(U used) {
		return new SimpleUsable<U>(used);
	}

	private HashMap<User, U> usedBy;
	private UseFlag useFlag;

	public final U useBy(UserInfo user) {
		return user.toUser().use(this);
	}

	public final Map<User, U> getUsedBy() {
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
	UseFlag getUseBy(UseCase useCase) {
		if (useCase.caseFlag(this.useFlag)) {
			return this.useFlag;
		}
		if (this.usedBy == null) {
			return useCase.unusedFlag();
		}

		UnknownUseFlag result = new UnknownUseFlag(useCase, this);

		this.useFlag = result;
		for (User user : this.usedBy.keySet()) {

			final UseFlag flag = user.getUseBy(useCase);
			final UnknownUseFlag unknown = flag.toUnknown();

			if (unknown == null) {
				if (flag.isUsed()) {
					return this.useFlag = result.setTrue();
				}
				continue;
			}

			this.useFlag = result = result.dependsOn(unknown);
		}

		return this.useFlag = result.end(this);
	}

	final U useBy(User user) {
		if (this.usedBy == null) {
			this.usedBy = new HashMap<User, U>(1);
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
