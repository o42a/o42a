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

import org.o42a.util.io.Source;


public final class LoggableSource implements Loggable, Cloneable {

	private final Source source;
	private LogReason reason;

	public LoggableSource(Source source) {
		this.source = source;
	}

	public final Source getSource() {
		return this.source;
	}

	@Override
	public final Loggable getLoggable() {
		return this;
	}

	@Override
	public final LogReason getReason() {
		return this.reason;
	}

	@Override
	public LoggableSource setReason(LogReason reason) {
		if (reason == null) {
			return this;
		}

		final LoggableSource clone = clone();

		if (this.reason == null) {
			clone.reason = reason;
		} else {
			clone.reason = this.reason.setNext(reason);
		}

		return clone;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {
		return visitor.visitSource(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append(this.source);
	}

	@Override
	public String toString() {
		if (this.source == null) {
			return super.toString();
		}
		return this.source.toString();
	}

	@Override
	protected LoggableSource clone() {
		try {
			return (LoggableSource) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
