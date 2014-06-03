/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.analysis.use.User;
import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Scope;
import org.o42a.core.ref.RefUsage;


public final class PathResolver implements UserInfo {

	public static PathResolver pathResolver(
			Scope pathStart,
			UserInfo user) {
		return new PathResolver(pathStart, user, null);
	}

	public static PathResolver fullPathResolver(
			Scope pathStart,
			UserInfo user,
			RefUsage usage) {
		return new PathResolver(pathStart, user, usage);
	}

	private final Scope pathStart;
	private final User<?> user;
	private final RefUsage usage;

	private PathResolver(Scope pathStart, UserInfo user, RefUsage usage) {
		this.pathStart = pathStart;
		this.user = user.toUser();
		this.usage = usage;
	}

	public final Scope getPathStart() {
		return this.pathStart;
	}

	public final boolean isFullResolution() {
		return this.usage != null;
	}

	@Override
	public final User<?> toUser() {
		return this.user;
	}

	public final RefUsage refUsage() {
		return this.usage;
	}

	public final PathResolver resolveBy(UserInfo user) {
		if (user.is(this.user)) {
			return this;
		}
		return new PathResolver(getPathStart(), user, this.usage);
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		if (isFullResolution()) {
			out.append("FullPathResolver[");
			out.append(this.usage).append(" by ");
		} else {
			out.append("PathResolver[");
		}
		out.append(this.user);
		out.append(']');

		return out.toString();
	}

}
