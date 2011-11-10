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


public final class LoggableData extends AbstractLoggable<LoggableData> {

	private final Object data;

	public LoggableData(Object data) {
		this.data = data;
	}

	public final Object getData() {
		return this.data;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {
		return visitor.visitData(this, p);
	}

	@Override
	public void print(StringBuilder out) {
		out.append(this.data);
	}

	@Override
	public String toString() {
		if (this.data == null) {
			return "null";
		}
		return this.data.toString();
	}

}
