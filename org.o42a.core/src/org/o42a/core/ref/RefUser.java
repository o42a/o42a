/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ref;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.analysis.use.User;
import org.o42a.analysis.use.UserInfo;


public final class RefUser implements UserInfo {

	private static final RefUser DUMMY_REF_USER = new RefUser(dummyUser());

	public static RefUser dummyRefUser() {
		return DUMMY_REF_USER;
	}

	public static RefUser refUser(UserInfo user) {

		final User<?> usr = user.toUser();

		if (usr.isDummy()) {
			return dummyRefUser();
		}

		return new RefUser(usr);
	}

	public static RefUser rtRefUser(UserInfo user, UserInfo rtUser) {

		final User<?> usr = user.toUser();

		if (usr.isDummy()) {
			return dummyRefUser();
		}

		return new RefUser(usr, rtUser.toUser());
	}

	private final User<?> user;
	private final User<?> rtUser;

	RefUser(User<?> user) {
		this.user = user;
		this.rtUser = dummyUser();
	}

	RefUser(User<?> user, User<?> rtUser) {
		this.user = user;
		this.rtUser = rtUser;
	}

	public final boolean hasRtUser() {
		return !rtUser().toUser().isDummy();
	}

	@Override
	public final User<?> toUser() {
		return this.user.toUser();
	}

	public final User<?> rtUser() {
		return this.rtUser.toUser();
	}

	@Override
	public String toString() {
		if (this.rtUser == null) {
			return super.toString();
		}
		if (!hasRtUser()) {
			return this.user.toString();
		}
		return this.user + "(at run time: " + this.rtUser + ')';
	}

}
