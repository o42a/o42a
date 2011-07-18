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

	private Loggable previous;

	public AbstractLoggable() {
	}

	@Override
	public Loggable getPreviousLoggable() {
		return this.previous;
	}

	@SuppressWarnings("unchecked")
	@Override
	public L setPreviousLoggable(Loggable previous) {
		if (previous == null) {
			return (L) this;
		}

		final L clone = clone();

		if (this.previous == null) {
			clone.previous = previous;
		} else {
			clone.previous = this.previous.setPreviousLoggable(previous);
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
