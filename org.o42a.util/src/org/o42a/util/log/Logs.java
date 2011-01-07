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


public final class Logs {

	private static final StartVisitor START_VISITOR = new StartVisitor();

	public static LoggablePosition start(LogInfo loggable) {
		return loggable.getLoggable().accept(START_VISITOR, null);
	}

	private Logs() {
	}

	private static final class StartVisitor
			implements LoggableVisitor<LoggablePosition, Void> {

		@Override
		public LoggablePosition visitPosition(
				LoggablePosition position,
				Void p) {
			return position;
		}

		@Override
		public LoggablePosition visitRange(LoggableRange range, Void p) {
			return range.getStart();
		}

		@Override
		public LoggablePosition visitData(LoggableData data, Void p) {
			return null;
		}

	}

}
