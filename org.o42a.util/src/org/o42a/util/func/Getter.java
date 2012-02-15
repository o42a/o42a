/*
    Utilities
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.util.func;


public abstract class Getter<T> {

	public static <T> Getter<? extends T> valueGetter(T value) {
		return new ValueGetter<T>(value);
	}

	public abstract T get();

	private static final class ValueGetter<T> extends Getter<T> {

		private final T value;

		ValueGetter(T value) {
			this.value = value;
		}

		@Override
		public final T get() {
			return this.value;
		}

		@Override
		public String toString() {
			return String.valueOf(this.value);
		}

	}

}
