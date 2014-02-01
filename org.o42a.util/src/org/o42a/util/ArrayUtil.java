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
package org.o42a.util;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;


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

	public static <T> T[] append(T[] array, T item) {

		final T[] newArray = copyOf(array, array.length + 1);

		newArray[array.length] = item;

		return newArray;
	}

	public static <T> T[] append(T[] array, T[] items) {
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

	public static <T> T[] append(T[] array, Collection<? extends T> items) {

		final int size = items.size();

		if (size == 0) {
			return array;
		}

		final T[] newArray = copyOf(array, array.length + size);
		int i = array.length;

		for (T item : items) {
			newArray[i++] = item;
		}

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

	public static <T> T[] remove(T[] array, int index) {
		return remove(array, index, index + 1);
	}

	public static <T> T[] remove(T[] array, int from, int to) {
		assert to >= from :
			"Range end is smaller the range start";

		@SuppressWarnings("unchecked")
		final T[] newArray = (T[]) Array.newInstance(
				array.getClass().getComponentType(),
				array.length - (to - from));


		arraycopy(array, 0, newArray, 0, from);
		arraycopy(array, to, newArray, from, array.length - to);

		return newArray;
	}

	public static <T> T[] replace(
			T[] array,
			int from,
			int to,
			T[] replacement) {

		assert to >= from :
			"Range end is smaller the range start";

		@SuppressWarnings("unchecked")
		final T[] newArray = (T[]) Array.newInstance(
				array.getClass().getComponentType(),
				array.length - (to - from) + replacement.length);

		arraycopy(array, 0, newArray, 0, from);
		arraycopy(replacement, 0, newArray, from, replacement.length);
		arraycopy(
				array,
				to,
				newArray,
				from + replacement.length,
				array.length - to);

		return newArray;
	}

	private ArrayUtil() {
	}

}
