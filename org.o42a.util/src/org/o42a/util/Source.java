/*
    Utilities
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
package org.o42a.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.log.LoggableData;


public abstract class Source implements LogInfo, Serializable {

	private static final long serialVersionUID = -1944519057349719722L;

	private final LoggableData loggableData = new LoggableData(this);

	public abstract String getName();

	public abstract Reader open() throws IOException;

	@Override
	public Loggable getLoggable() {
		return this.loggableData;
	}

	@Override
	public String toString() {
		return getName();
	}

}
