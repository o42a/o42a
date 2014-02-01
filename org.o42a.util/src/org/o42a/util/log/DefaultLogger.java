/*
    Utilities
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static java.util.logging.Logger.getAnonymousLogger;

import java.util.Formatter;
import java.util.logging.Level;


final class DefaultLogger implements Logger {

	@Override
	public void log(LogRecord record) {

		final Formatter f = new Formatter();

		try {
			f.format("%s", record);
			getAnonymousLogger().log(level(record.getSeverity()), f.toString());
		} finally {
			f.close();
		}
	}

	static Level level(Severity severity) {

		final Level level;

		switch (severity) {
		case WARNING:
			level = Level.WARNING;
			break;
		case INFO:
			level = Level.INFO;
			break;
		case TRACE:
			level = Level.FINE;
			break;
		case VERBOSE:
			level = Level.ALL;
			break;
		case FATAL:
		case ERROR:
		default:
			level = Level.SEVERE;
		}

		return level;
	}

}
