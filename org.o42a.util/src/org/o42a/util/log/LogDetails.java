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

import java.util.*;


public class LogDetails extends Loggable implements Iterable<LogDetail> {

	private final LogLocation location;
	private final Map<LogDetail, LogLocation> details;

	LogDetails(LogLocation location, Map<LogDetail, LogLocation> details) {
		this.location = location;
		this.details = details;
	}

	@Override
	public final LogLocation getLocation() {
		return this.location;
	}

	@Override
	public LogDetails addDetail(LogDetail detail, LogInfo location) {
		assert detail != null :
			"Detail not specified";
		assert location != null :
			"Location not specified";

		final LogLocation newLocation = location.getLoggable().getLocation();
		final LogLocation oldLocation = this.details.get(detail);
		final HashMap<LogDetail, LogLocation> details;

		if (oldLocation == null) {
			details = new HashMap<>(this.details.size() + 1);
		} else if (oldLocation.equals(newLocation)) {
			return this;
		} else {

			final int oldSize = this.details.size();

			if (oldSize <= 1) {
				return new LogDetails(
						this.location,
						singletonMap(detail, newLocation));
			}

			details = new HashMap<>(oldSize);
		}

		details.putAll(this.details);
		details.put(detail, newLocation);

		return new LogDetails(this.location, details);
	}

	@Override
	public final LogLocation detail(LogDetail detail) {
		return this.details.get(detail);
	}

	@Override
	public final Iterator<LogDetail> iterator() {
		return this.details.keySet().iterator();
	}

	@Override
	public final LogDetails toDetails() {
		return this;
	}

	@Override
	public void print(StringBuilder out) {
		getLocation().print(out);
		out.append(" (");

		boolean comma = false;

		for (Map.Entry<LogDetail, LogLocation> e : this.details.entrySet()) {
			if (!comma) {
				comma = true;
			} else {
				out.append(", ");
			}
			out.append(e.getKey().getMessage()).append(" is here: ");
			e.getValue().print(out);
		}

		out.append(')');
	}

	@Override
	public void formatTo(
			Formatter formatter,
			int flags,
			int width,
			int precision) {
		formatter.format("%s (", getLocation());

		boolean comma = false;

		for (Map.Entry<LogDetail, LogLocation> e : this.details.entrySet()) {

			final String format;

			if (!comma) {
				format = "%s is here: %s";
			} else {
				format = ", %s is here: %s";
			}

			formatter.format(format, e.getKey().getMessage(), e.getValue());
		}

		formatter.format(")");
	}

}
