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

import org.o42a.core.Scope;
import org.o42a.util.use.*;


public final class PathResolver implements UserInfo {

	public static PathResolver pathResolver(
			Scope pathStart,
			UserInfo user) {
		return new PathResolver(pathStart, user.toUser(), (byte) -1);
	}

	public static PathResolver fullPathResolver(
			Scope pathStart,
			UserInfo user) {
		return new PathResolver(pathStart, user.toUser(), (byte) 0);
	}

	public static PathResolver valuePathResolver(
			Scope pathStart,
			UserInfo user) {
		return new PathResolver(pathStart, user.toUser(), (byte) 1);
	}

	private final Scope pathStart;
	private final User user;
	private final byte fullResolution;

	private PathResolver(Scope pathStart, User user, byte fullResolution) {
		this.pathStart = pathStart;
		this.user = user;
		this.fullResolution = fullResolution;
	}

	public final Scope getPathStart() {
		return this.pathStart;
	}

	public final boolean isFullResolution() {
		return this.fullResolution >= 0;
	}

	public final boolean isValueResolution() {
		return this.fullResolution > 0;
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		return this.user.getUseBy(useCase);
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return this.user.isUsedBy(useCase);
	}

	@Override
	public final User toUser() {
		return this.user;
	}

	public final PathResolver resolveBy(UserInfo user) {
		if (user.toUser() == this.user) {
			return this;
		}
		return new PathResolver(
				getPathStart(),
				user.toUser(),
				this.fullResolution);
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		if (isFullResolution()) {
			if (isValueResolution()) {
				out.append("Value");
			} else {
				out.append("Full");
			}
		}
		out.append("PathResolver[").append(this.user).append(']');

		return out.toString();
	}

}
