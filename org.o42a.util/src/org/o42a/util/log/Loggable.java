/*
    Utilities
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
package org.o42a.util.log;

import java.util.Formattable;


public abstract class Loggable implements LogInfo, Formattable {

	Loggable() {
	}

	@Override
	public final Loggable getLoggable() {
		return this;
	}

	public abstract LogLocation getLocation();

	public abstract LogDetails addDetail(LogDetail detail, LogInfo location);

	public final LogDetails addDetailLocation(
			LogDetail detail,
			LogInfo location) {
		return addDetail(detail, location.getLoggable().detailLocation(detail));
	}

	public abstract LogDetails toDetails();

	public abstract void print(StringBuilder out);

	public Loggable detail(LogDetail detail) {

		final LogDetails details = toDetails();

		if (details == null) {
			return null;
		}

		return details.detail(detail);
	}

	public final Loggable detailLocation(LogDetail detail) {

		final Loggable detailLocation = detail(detail);

		if (detailLocation != null) {
			return detailLocation;
		}

		return this;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		print(out);

		return out.toString();
	}

}
