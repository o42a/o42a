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


public class LoggableData implements Loggable, Cloneable {

	private final Object loggableData;
	private Loggable previous;

	public LoggableData(Object loggableData) {
		if (loggableData.getClass().getSimpleName().equals("Location")) {
			throw new NullPointerException();
		}
		this.loggableData = loggableData;
	}

	public LoggableData(Object loggableData, Loggable previous) {
		this.loggableData = loggableData;
		this.previous = previous;
	}

	public final Object getLoggableData() {
		return this.loggableData;
	}

	@Override
	public Loggable getLoggable() {
		return this;
	}

	@Override
	public Loggable getPreviousLoggable() {
		return this.previous;
	}

	@Override
	public LoggableData setPreviousLoggable(Loggable previous) {
		if (previous == null) {
			return this;
		}

		final LoggableData clone = clone();

		if (this.previous == null) {
			clone.previous = previous;
		} else {
			clone.previous = this.previous.setPreviousLoggable(previous);
		}

		return clone;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {
		return visitor.visitData(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append(this.loggableData);
	}

	@Override
	public String toString() {
		return this.loggableData != null
		? this.loggableData.toString() : "null";
	}

	@Override
	protected LoggableData clone() {
		try {
			return (LoggableData) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
