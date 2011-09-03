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

import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.use.*;


public final class PathResolver implements LocationInfo, UserInfo {

	public static PathResolver pathResolver(
			LocationInfo location,
			UserInfo user) {
		return new PathResolver(location, user.toUser(), (byte) -1);
	}

	public static PathResolver fullPathResolver(
			LocationInfo location,
			UserInfo user) {
		return new PathResolver(location, user.toUser(), (byte) 0);
	}

	public static PathResolver valuePathResolver(
			LocationInfo location,
			UserInfo user) {
		return new PathResolver(location, user.toUser(), (byte) 1);
	}

	private final LocationInfo location;
	private final User user;
	private final byte fullResolution;

	private PathResolver(
			LocationInfo location,
			User user,
			byte fullResolution) {
		this.location = location;
		this.user = user;
		this.fullResolution = fullResolution;
	}

	public final boolean isFullResolution() {
		return this.fullResolution >= 0;
	}

	public final boolean isValueResolution() {
		return this.fullResolution > 0;
	}

	@Override
	public final Loggable getLoggable() {
		return this.location.getLoggable();
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

	@Override
	public final CompilerContext getContext() {
		return this.location.getContext();
	}

	public final PathResolver resolveBy(UserInfo user) {
		if (user.toUser() == this.user) {
			return this;
		}
		return new PathResolver(
				this.location,
				user.toUser(),
				this.fullResolution);
	}

	@Override
	public String toString() {
		if (this.user == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("PathResolver[").append(this.location);
		if (!this.user.isDummy()) {
			out.append(" by ").append(this.user);
		}
		out.append(']');

		return out.toString();
	}

}
