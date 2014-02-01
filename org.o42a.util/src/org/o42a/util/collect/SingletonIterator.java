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

import java.util.NoSuchElementException;


final class SingletonIterator<T> extends ReadonlyIterator<T> {

	private final T element;
	private boolean iterated;

	SingletonIterator(T value) {
		this.element = value;
	}

	@Override
	public boolean hasNext() {
		return !this.iterated;
	}

	@Override
	public T next() {
		if (!this.iterated) {
			this.iterated = true;
			return this.element;
		}
		throw new NoSuchElementException();
	}

	@Override
	public String toString() {
		return String.valueOf(this.element);
	}

}
