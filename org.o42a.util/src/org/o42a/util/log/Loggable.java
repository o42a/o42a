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

import java.util.Formattable;


public abstract class Loggable implements LogInfo, Formattable, Cloneable {

	private LogReason reason;

	@Override
	public final Loggable getLoggable() {
		return this;
	}

	public final LogReason getReason() {
		return this.reason;
	}

	public Loggable setReason(LogReason reason) {
		if (reason == null) {
			return this;
		}

		final Loggable clone = clone();

		if (this.reason == null) {
			clone.reason = reason;
		} else {
			clone.reason = this.reason.setNext(reason);
		}

		return clone;
	}

	public abstract void print(StringBuilder out);

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		print(out);

		return out.toString();
	}

	@Override
	protected Loggable clone() {
		try {
			return (Loggable) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
