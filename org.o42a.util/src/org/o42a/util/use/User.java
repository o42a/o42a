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

	private HashSet<Use<?>> userOf;
	private UseFlag useFlag;

	public final boolean using() {
		return this.userOf != null && !this.userOf.isEmpty();
	}

	public final Set<Use<?>> getUserOf() {
		if (this.userOf == null) {
			return emptySet();
		}
		return this.userOf;
	}

	public void releaseUses() {

		final HashSet<Use<?>> uses = this.userOf;

		if (uses == null) {
			return;
		}

		this.userOf = null;
		for (Use<?> use : this.userOf) {
			use.release();
		}
		uses.clear();
	}

	@Override
	public final User toUser() {
		return this;
	}

	public boolean usedBy(UseCase useCase) {
		if (useCase.caseFlag(this.useFlag)) {
			return this.useFlag.isUsed();
		}

		final boolean result = determineUseBy(useCase);

		this.useFlag = useCase.useFlag(result);

		return result;
	}

	protected abstract boolean determineUseBy(UseCase useCase);

	<U> Use<U> use(Usable<U> usable) {

		final Use<U> use = usable.useBy(this);

		if (this.userOf == null) {
			this.userOf = new HashSet<Use<?>>();
		}
		this.userOf.add(use);

		return use;
	}

	void release(Use<?> use) {
		if (this.userOf == null) {
			return;
		}
		this.userOf.remove(use);
	}

}
