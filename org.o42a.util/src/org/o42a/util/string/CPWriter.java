/*
    Utilities
    Copyright (C) 2012-2014 Ruslan Lopatin

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


public abstract class CPWriter {

	/**
	 * Expands a writer capacity.
	 *
	 * @param size an estimated number of characters to be written.
	 */
	public void expandCapacity(int size) {
	}

	public final CPWriter write(String string) {

		final int len = string.length();

		if (len == 0) {
			return this;
		}

		expandCapacity(len);

		int i = 0;

		do {

			final int cp = string.codePointAt(i);

			i += Character.charCount(cp);
			writeCodePoint(cp);
		} while (i < len);

		return this;
	}

	public abstract void writeCodePoint(int codePoint);

}
