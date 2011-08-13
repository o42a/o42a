/*
    Utilities
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
package org.o42a.util.log;

import java.util.Formattable;
import java.util.Formatter;

import org.o42a.util.io.Source;


final class LoggableFormatter implements LoggableVisitor<Void, Formatter> {

	private static final LoggableFormatter LOGGABLE_FORMATTER =
			new LoggableFormatter();

	static void format(Formatter formatter, Loggable loggable) {
		if (loggable instanceof Formattable) {
			formatter.format(" at %s", loggable);
		} else if (loggable != null) {
			loggable.accept(LOGGABLE_FORMATTER, formatter);
		}
	}

	@Override
	public Void visitSource(LoggableSource source, Formatter p) {
		p.format(" at %s", source.getSource());
		return null;
	}

	@Override
	public Void visitPosition(LoggablePosition position, Formatter p) {

		final StringBuilder out = new StringBuilder();

		LogRecord.formatPosition(out, position, true);
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

		LogRecord.formatPosition(out, start, true);
		out.append("..");
		LogRecord.formatPosition(out, end, withSource);

		p.format(" at %s", out);

		return null;
	}

	@Override
	public Void visitData(LoggableData data, Formatter p) {
		p.format(" at %s", data.getData());
		return null;
	}

}
