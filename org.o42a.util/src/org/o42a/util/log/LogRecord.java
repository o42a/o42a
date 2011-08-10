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
package org.o42a.util.log;

import java.io.Serializable;
import java.util.Formattable;
import java.util.Formatter;
import java.util.IllegalFormatException;

import org.o42a.util.io.Source;


public class LogRecord implements Formattable, Serializable {

	private static final long serialVersionUID = 4342439372674659502L;

	private static final Object[] NO_ARGS = new Object[0];
	private static final LoggableFormatter LOGGABLE_FORMATTER =
			new LoggableFormatter();

	private final Severity severity;
	private final Object source;
	private final String code;
	private final String message;
	private final Loggable loggable;
	private final Object[] args;

	public LogRecord(
			Object source,
			Severity severity,
			String code,
			String message,
			Loggable loggable,
			Object... args) {
		this.severity = severity;
		this.source = source;
		this.code = code != null ? code : severity.toString();
		this.message = message != null ? message : severity.toString();
		this.loggable = loggable;
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

	public Loggable getLoggable() {
		return this.loggable;
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

		final Loggable loggable = getLoggable();

		if (loggable == null) {
			return;
		}
		if (loggable instanceof Formattable) {
			formatter.format(" at %s", loggable);
		}
		format(formatter, loggable);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Formatter formatter = new Formatter(out);

		formatter.format("%s", this);

		return out.toString();
	}

	private static void format(Formatter formatter, Loggable loggable) {
		loggable.accept(LOGGABLE_FORMATTER, formatter);
	}

	public static void formatPosition(
			StringBuilder out,
			LoggablePosition position,
			boolean withFile) {
		if (withFile) {

			final Source source = position.source();

			if (source != null) {
				out.append(source.getName()).append(':');
			}
		}

		out.append(position.line()).append(',').append(position.column());
		out.append('(').append(position.offset()).append(')');
	}

	private static final class LoggableFormatter
			implements LoggableVisitor<Void, Formatter> {

		@Override
		public Void visitSource(LoggableSource source, Formatter p) {
			p.format(" at %s", source.getSource());
			return null;
		}

		@Override
		public Void visitPosition(LoggablePosition position, Formatter p) {

			final StringBuilder out = new StringBuilder();

			formatPosition(out, position, true);
			p.format(" at %s", out);

			return null;
		}

		@Override
		public Void visitRange(LoggableRange range, Formatter p) {

			final StringBuilder out = new StringBuilder();
			final LoggablePosition start = range.getStart();
			final LoggablePosition end = range.getEnd();
			final Source src1 = start.source();
			final Source src2 = end.source();
			final boolean withSource;

			if (src1 == null) {
				withSource = src2 != null;
			} else {
				withSource = !src1.equals(src2);
			}

			formatPosition(out, start, true);
			out.append("..");
			formatPosition(out, end, withSource);

			p.format(" at %s", out);

			return null;
		}

		@Override
		public Void visitData(LoggableData data, Formatter p) {
			p.format(" at %s", data.getData());
			return null;
		}

	}

}
