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
import java.util.NoSuchElementException;


public abstract class SubIterator<T, B> extends ReadonlyIterator<T> {

	private final Iterator<B> base;
	private Iterator<? extends T> nested;

	public SubIterator(Iterator<B> base) {
		assert base != null :
			"Base iterator not specified";
		this.base = base;
	}

	@Override
	public final boolean hasNext() {
		if (this.nested == null || !this.nested.hasNext()) {
			return nextImpl();
		}
		return true;
	}

	@Override
	public final T next() {
		if (this.nested == null || !this.nested.hasNext()) {
			if (!nextImpl()) {
				throw new NoSuchElementException();
			}
		}
		return this.nested.next();
	}

	/**
	 * Constructs a nested iterator for the given element of the base iterator.
	 *
	 * @param baseElement base iterator's element.
	 *
	 * @return nested iterator.
	 */
	protected abstract Iterator<? extends T> nestedIterator(B baseElement);

	private boolean nextImpl() {
		do {
			if (!this.base.hasNext()) {
				return false;
			}
			this.nested = nestedIterator(this.base.next());
		} while (this.nested == null || !this.nested.hasNext());
		return true;
	}

}
