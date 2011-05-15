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

import java.util.Set;


public abstract class User implements UserInfo, UseInfo {

	private static final DummyUser DUMMY_USER = new DummyUser("DummyUser");

	public static User dummyUser() {
		return DUMMY_USER;
	}

	public static User dummyUser(String name) {
		return new DummyUser(name);
	}

	public static UseCase useCase(String name) {
		return new UseCase(name);
	}

	User() {
	}

	public boolean isDummy() {
		return false;
	}

	public abstract Set<?> getUserOf();

	@Override
	public final User toUser() {
		return this;
	}

	public final boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
	}

	abstract <U> U use(Usable<U> usable);

}
