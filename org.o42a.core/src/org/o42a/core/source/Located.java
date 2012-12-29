/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.source;

import org.o42a.util.log.LogInfo;


public abstract class Located implements LocationInfo {

	private final Location location;

	public Located(LocationInfo location) {
		this.location = location.getLocation();
	}

	public Located(CompilerContext context, LogInfo logInfo) {
		this.location = new Location(context, logInfo);
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

}