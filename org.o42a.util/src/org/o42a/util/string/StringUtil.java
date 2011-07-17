/*
    Utilities
    Copyright (C) 2011 Ruslan Lopatin

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

	public static String removeLeadingChars(String from, char what) {

		int i = 0;
		final int len = from.length();

		while (i < len) {
			if (from.charAt(i) != what) {
				return from.substring(i, from.length());
			}
			++i;
		}

		return "";
	}

	public static String removeTrailingChars(String from, char what) {

		int last = from.length() - 1;

		while (last >= 0 && from.charAt(last) == what) {
			--last;
		}

		return from.substring(0, last + 1);
	}

	private StringUtil() {
	}

}
