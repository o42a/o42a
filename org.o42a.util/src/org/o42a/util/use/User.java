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


public abstract class User implements UserInfo {

	private static final DummyUser DUMMY_USER = new DummyUser();

	public static User dummyUser() {
		return DUMMY_USER;
	}

	public static UseCase useCase(String name) {
		return new UseCase(name);
	}

	private HashSet<Object> userOf;
	private UseFlag useFlag;

	public final boolean using() {
		return this.userOf != null && !this.userOf.isEmpty();
	}

	public final Set<?> getUserOf() {
		if (this.userOf == null) {
			return emptySet();
		}
		return this.userOf;
	}

	@Override
	public final User toUser() {
		return this;
	}

	public final boolean isUsedBy(UseCase useCase) {

		final UseFlag flag = getUseBy(useCase);

		assert !flag.isUnknown() :
			"Can not determine whether " + this + " used by " + useCase;

		return flag.isUsed();
	}

	public final UseFlag getUseBy(UseCase useCase) {
		if (useCase.caseFlag(this.useFlag)) {
			return this.useFlag;
		}

		useCase.start(this);

		boolean ok = false;
		UseFlag result = this.useFlag = useCase.checkUseFlag();

		try {
			result = determineUseBy(useCase);
			ok = true;
		} finally {

			final boolean topLevel = useCase.end(this);

			if (!ok) {
				this.useFlag = null;
			} else if (!result.isUnknown()) {
				this.useFlag = result;
			} else if (topLevel) {
				this.useFlag = result = useCase.unusedFlag();
			} else {
				this.useFlag = null;
			}
		}

		return result;
	}

	protected abstract UseFlag determineUseBy(UseCase useCase);

	<U> U use(Usable<U> usable) {

		final U use = usable.useBy(this);

		if (this.userOf == null) {
			this.userOf = new HashSet<Object>();
		}
		this.userOf.add(use);

		return use;
	}

}
