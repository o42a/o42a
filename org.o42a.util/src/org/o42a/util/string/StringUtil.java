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
package org.o42a.util.string;


public final class StringUtil {

	public static final int indexOfDiff(String in, char what) {
		return indexOfDiff(in, what, 0);
	}

	public static int indexOfDiff(String in, char what, int from) {

		int first = from;
		final int len = in.length();

		while (first < len) {
			if (in.charAt(first) != what) {
				return first;
			}
			++first;
		}

		return -1;
	}

	public static final int lastIndexOfDiff(String in, char what) {
		return lastIndexOfDiff(in, what, in.length() - 1);
	}

	public static int lastIndexOfDiff(String in, char what, int from) {

		int last = from;

		while (last >= 0 && in.charAt(last) == what) {
			--last;
		}

		return last;
	}

	public static String removeLeadingChars(String from, char what) {

		final int first = indexOfDiff(from, what);

		if (first == 0) {
			return from;
		}
		if (first < 0) {
			return "";
		}

		return from.substring(first, from.length());
	}

	public static String removeTrailingChars(String from, char what) {

		final int last = lastIndexOfDiff(from, what);

		if (last == from.length() - 1) {
			return from;
		}
		if (last < 0) {
			return "";
		}

		return from.substring(0, last + 1);
	}

	private StringUtil() {
	}

}
