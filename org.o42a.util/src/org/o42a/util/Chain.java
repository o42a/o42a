/*
    Utilities
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;


public abstract class Chain<T> implements Iterable<T> {

	private T first;
	private T last;

	public <I extends T> I add(I item) {
		if (isEmpty()) {
			this.first = this.last = item;
		} else {
			setNext(this.last, item);
			this.last = item;
		}
		return item;
	}

	public final T getFirst() {
		return this.first;
	}

	public final T getLast() {
		return this.last;
	}

	public final boolean isEmpty() {
		return this.first == null;
	}

	public final void empty() {
		this.first = this.last = null;
	}

	@Override
	public Iterator<T> iterator() {
		if (isEmpty()) {
			return Collections.<T>emptyList().iterator();
		}
		return new Iter();
	}

	protected abstract T next(T item);

	protected abstract void setNext(T prev, T next);

	@Override
	public String toString() {
		if (isEmpty()) {
			return "[]";
		}

		final StringBuilder out = new StringBuilder();
		out.append('[');
		T item = this.first;
		boolean comma = false;

		while (item != null) {
			if (comma) {
				out.append(", ");
			} else {
				comma = true;
			}
			out.append(item);
			item = next(item);
		}
		out.append(']');

		return out.toString();
	}

	private final class Iter implements Iterator<T> {

		private T next = Chain.this.first;

		@Override
		public boolean hasNext() {
			return this.next != null;
		}

		@Override
		public T next() {

			final T result = this.next;

			if (result == null) {
				throw new NoSuchElementException();
			}

			this.next = Chain.this.next(result);

			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Can not remove items from chain");
		}

	}

}
