/*
    Compiler Core
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
package org.o42a.core.ref.path;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.util.use.UserInfo;


final class AbsolutePathTracker extends PathTracker {

	private int beforeStart;

	AbsolutePathTracker(UserInfo user, PathWalker walker, int startIndex) {
		super(user, walker);
		this.beforeStart = startIndex;
	}

	@Override
	public UserInfo nextUser() {
		if (this.beforeStart > 0) {
			return dummyUser();
		}
		return super.nextUser();
	}

	@Override
	boolean walk(boolean succeed) {
		--this.beforeStart;
		return super.walk(succeed);
	}

}
