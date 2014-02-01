/*
    Utilities
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.util.log;

import static java.util.Collections.singletonMap;

import org.o42a.util.io.Source;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.io.SourceRange;


public abstract class LogLocation extends Loggable {

	@Override
	public final LogLocation getLocation() {
		return this;
	}

	public abstract Source getSource();

	public abstract SourcePosition getPosition();

	public abstract SourceRange getRange();

	@Override
	public final LogDetails addDetail(LogDetail detail, LogInfo location) {
		return new LogDetails(
				this,
				singletonMap(detail, location.getLoggable().getLocation()));
	}

	@Override
	public final LogDetails toDetails() {
		return null;
	}

}
