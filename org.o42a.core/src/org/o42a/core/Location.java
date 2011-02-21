/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core;

import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.log.LoggableData;


public class Location implements LocationInfo {

	private final CompilerContext context;
	private final Loggable loggable;

	public Location(LocationInfo location) {
		assert location != null :
			"Location not specified";
		this.context = location.getContext();
		this.loggable = location.getLoggable();
	}

	public Location(CompilerContext context, LogInfo logInfo) {
		assert context != null :
			"Compiler context not specified";
		this.context = context;
		this.loggable =
			logInfo != null ? logInfo.getLoggable() : new LoggableData(this);
	}

	@Override
	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Loggable getLoggable() {
		return this.loggable;
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	public final LocationInfo locationFor(String path) {
		return getContext().locationFor(this, path);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName()).append('[');
		out.append(this.context);

		final Loggable loggable = getLoggable();

		if (loggable != null) {
			out.append("]:");
			loggable.printContent(out);
		} else {
			out.append(']');
		}

		return out.toString();
	}

}
