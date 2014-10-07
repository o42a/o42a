/*
    Utilities
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static java.util.Collections.emptyIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;


/**
 * A single-linked items chain.
 *
 * <p>Each item of the chain should contain the next one.</p>
 *
 * @param <T> item type.
 */
public class Chain<T> implements Iterable<T> {

	private final UnaryOperator<T> getNext;
	private final BiConsumer<T, T> setNext;
	private T first;
	private T last;

	public Chain(UnaryOperator<T> getNext, BiConsumer<T, T> setNext) {
		this.getNext = getNext;
		this.setNext = setNext;
	}

	/**
	 * Appends the last item of the chain.
	 *
	 * @param item item to add.
	 *
	 * @return added item.
	 */
	public <I extends T> I add(I item) {
		if (isEmpty()) {
			this.first = this.last = item;
		} else {
			setNext(this.last, item);
			this.last = item;
		}
		return item;
	}

	/**
	 * Adds the first item to chain.
	 *
	 * @param item item to add.
	 *
	 * @return added item.
	 */
	public <I extends T> I push(I item) {
		if (isEmpty()) {
			this.first = this.last = item;
		} else {
			setNext(item, this.first);
			this.first = item;
		}
		return item;
	}

	/**
	 * Removes the first item from the chain.
	 *
	 * @return removed item or <code>null</code> for empty chain.
	 */
	public T pop() {

		final T first = this.first;

		if (first != null) {
			this.first = next(first);
		}

		return first;
	}

	/**
	 * The first item of this chain.
	 *
	 * @return the first item or <code>null</code> for empty chain.
	 */
	public final T getFirst() {
		return this.first;
	}

	/**
	 * The last item of this chain.
	 *
	 * @return the last item or <code>null</code> for empty chain.
	 */
	public final T getLast() {
		return this.last;
	}

	/**
	 * Checks whether this chain is empty.
	 *
	 * @return <code>true</code> is this chain contains no items,
	 *  or <code>false</code> otherwise.
	 */
	public final boolean isEmpty() {
		return this.first == null;
	}

	/**
	 * Checks whether this chain contains the given item.
	 *
	 * @param item item to find.
	 *
	 * @return <code>true</code> if this chain contains an item equal to the
	 * given one, or <code>false</code> otherwise.
	 */
	public final boolean contains(T item) {
		for (T t = getFirst(); t != null; t = next(t)) {
			if (t.equals(item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clears this chain by removing all items from it.
	 */
	public final void clear() {
		this.first = this.last = null;
	}

	@Override
	public Iterator<T> iterator() {
		if (isEmpty()) {
			return emptyIterator();
		}
		return new Iter();
	}

	private final T next(T t) {
		return this.getNext.apply(t);
	}

	private final void setNext(T item, T next) {
		this.setNext.accept(item, next);
	}

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

	}

}
