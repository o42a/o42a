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


public final class DetailedLogger implements Logger {

	private final Logger logger;
	private final LogDetail detail;
	private final LogInfo location;

	public DetailedLogger(Logger logger, LogDetail detail, LogInfo location) {
		assert logger != null :
			"Logger not specified";
		assert detail != null :
			"Detail not specified";
		assert location != null :
			"Location not specified";
		this.logger = logger;
		this.detail = detail;
		this.location = location;
	}

	@Override
	public void log(LogRecord record) {

		final LogMessage message = record.getMessage();

		this.logger.log(new LogRecord(
				message.getSeverity(),
				message.getCode(),
				message.getText(),
				record.getLoggable().addDetail(this.detail, this.location),
				record.getArgs()));
	}

	@Override
	public String toString() {
		if (this.logger == null) {
			return super.toString();
		}
		return this.logger.toString();
	}

}
