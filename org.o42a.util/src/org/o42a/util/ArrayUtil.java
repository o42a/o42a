/*
    Utilities
    Copyright (C) 2010 Ruslan Lopatin

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

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;


public class ArrayUtil {

	public static final <T> T[] clip(T[] array, int length) {
		if (array.length <= length) {
			return array;
		}

		return Arrays.copyOf(array, length);
	}

	public static final <T> T[] expand(T[] array, int length) {
		if (array.length >= length) {
			return array;
		}

		return Arrays.copyOf(array, length);
	}

	public static <T> T[] append(T[] array, T... items) {
		if (items.length == 0) {
			return array;
		}
		if (array.length == 0) {
			return items;
		}

		final T[] newArray = copyOf(array, array.length + items.length);

		arraycopy(items, 0, newArray, array.length, items.length);

		return newArray;
	}

	public static <T> T[] prepend(T item, T[] array) {

		@SuppressWarnings("unchecked")
		final T[] newArray = (T[]) Array.newInstance(
				array.getClass().getComponentType(),
				1 + array.length);

		newArray[0] = item;
		arraycopy(array, 0, newArray, 1, array.length);

		return newArray;
	}

	public static <T> void append(ArrayList<T> list, T... items) {
		list.ensureCapacity(list.size() + items.length);
		for (T item : items) {
			list.add(item);
		}
	}

	public static <T> T[] remove(T[] array, int index) {

		@SuppressWarnings("unchecked")
		final T[] newArray = (T[]) Array.newInstance(
				array.getClass().getComponentType(),
				array.length - 1);


		arraycopy(array, 0, newArray, 0, index);
		arraycopy(array, index + 1, newArray, index, newArray.length - index);

		return newArray;
	}

	private ArrayUtil() {
	}

}
