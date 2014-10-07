/*
    Utilities
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.util.collect;

import java.util.Iterator;


final class CombinedIterator<T> implements Iterator<T> {

	private final Iterator<? extends T> first;
	private final Iterator<? extends T> second;
	private boolean useSecond;

	CombinedIterator(
			Iterator<? extends T> first,
			Iterator<? extends T> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean hasNext() {
		return nonEmpty().hasNext();
	}

	@Override
	public T next() {
		return nonEmpty().next();
	}

	@Override
	public String toString() {
		if (this.second == null) {
			return super.toString();
		}
		return "CombinedIterator[" + this.first + ", " + this.second + ']';
	}

	private Iterator<? extends T> nonEmpty() {
		if (!this.useSecond) {
			if (this.first.hasNext()) {
				return this.first;
			}
			this.useSecond = true;
		}
		return this.second;
	}

}
