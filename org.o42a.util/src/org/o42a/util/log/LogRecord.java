/*
    Utilities
    Copyright (C) 2010 Ruslan Lopatin

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

import java.io.Serializable;
import java.util.Formattable;
import java.util.Formatter;
import java.util.IllegalFormatException;


public class LogRecord implements Formattable, Serializable {

	private static final long serialVersionUID = 572665045518100529L;

	private static final Object[] NO_ARGS = new Object[0];

	private final Severity severity;
	private final Object source;
	private final String code;
	private final String message;
	private final Object location;
	private final Object[] args;

	public LogRecord(
			Object source,
			Severity severity,
			String code,
			String message,
			Object location,
			Object... args) {
		this.severity = severity;
		this.source = source;
		this.code = code != null ? code : severity.toString();
		this.message = message != null ? message : severity.toString();
		this.location = location;
		this.args = args != null ? args : NO_ARGS;
	}

	public Object getSource() {
		return this.source;
	}

	public Severity getSeverity() {
		return this.severity;
	}

	public String getCode() {
		return this.code;
	}

	public Object getLocation() {
		return this.location;
	}

	public String getMessage() {
		return this.message;
	}

	public Object[] getArgs() {
		return this.args;
	}

	@Override
	public void formatTo(
			Formatter formatter,
			int flags,
			int width,
			int precision)
	throws IllegalFormatException {

		final Object source = getSource();

		if (source != null) {
			formatter.format("%s: ", source);
		}

		formatter.format(getMessage(), getArgs());

		final Object location = getLocation();

		if (location != null) {
			formatter.format(" at %s", location);
		}

	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Formatter formatter = new Formatter(out);

		formatter.format("%s", this);

		return out.toString();
	}

}
