/*
    Utilities
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.util.log.LogRecord.NO_ARGS;

import java.util.Formattable;
import java.util.Formatter;


public class LogReason implements LogInfo, Formattable, Cloneable {

	private final String code;
	private final String message;
	private final Loggable loggable;
	private final Object[] args;
	private LogReason next;

	public LogReason(
			String code,
			String message,
			LogInfo loggable,
			Object... args) {
		assert code != null :
			"Code not specified";
		assert message != null :
			"Message not specified";
		assert loggable != null :
			"Location not specified";
		this.code = code;
		this.message = message;
		this.loggable = loggable.getLoggable();
		this.args = args != null ? args : NO_ARGS;
	}

	public final String getCode() {
		return this.code;
	}

	public final String getMessage() {
		return this.message;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	public final Object[] getArgs() {
		return this.args;
	}

	public final LogReason getNext() {
		return this.next;
	}

	public final LogReason setNext(LogReason next) {

		final LogReason clone = clone();

		if (this.next == null) {
			clone.next = next;
		} else {
			clone.next = this.next.setNext(next);
		}

		return clone;
	}

	@Override
	public void formatTo(
			Formatter formatter,
			int flags,
			int width,
			int precision) {
		formatter.format(getMessage(), getArgs());
		formatter.format(" %s", this.loggable);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Formatter formatter = new Formatter(out);

		formatter.format("%s", this);

		return out.toString();
	}

	@Override
	protected LogReason clone() {
		try {
			return (LogReason) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
