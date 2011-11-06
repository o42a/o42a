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


public abstract class AbstractLoggable<L extends AbstractLoggable<L>>
		implements Loggable, Cloneable {

	private LogReason reason;

	public AbstractLoggable() {
	}

	@Override
	public final Loggable getLoggable() {
		return this;
	}

	@Override
	public final LogReason getReason() {
		return this.reason;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final L setReason(LogReason reason) {
		if (reason == null) {
			return (L) this;
		}

		final L clone = clone();

		if (this.reason == null) {
			clone.reason = reason;
		} else {
			clone.reason = this.reason.setNext(reason);
		}

		return clone;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected L clone() {
		try {
			return (L) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
