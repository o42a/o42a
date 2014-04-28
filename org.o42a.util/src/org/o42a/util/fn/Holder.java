/*
    Utilities
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.util.fn;

import java.util.function.Supplier;


public final class Holder<T> implements Supplier<T> {

	public static <T> Holder<T> holder(T value) {
		return new Holder<>(value);
	}

	private final T value;

	public Holder(T value) {
		this.value = value;
	}

	@Override
	public final T get() {
		return this.value;
	}

	@Override
	public int hashCode() {
		return this.value == null ? 0 : this.value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Holder<?> other = (Holder<?>) obj;

		if (this.value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!this.value.equals(other.value)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return "null";
		}
		return this.value.toString();
	}

}
