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
package org.o42a.util.io;

import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.toCodePoint;

import java.io.IOException;


final class StringSourceReader extends SourceReader {

	private final String string;
	private int offset;

	StringSourceReader(StringSource source) {
		super(source);
		this.string = source.getString();
	}

	@Override
	public long offset() {
		return this.offset;
	}

	@Override
	public void seek(long offset) throws IOException {
		if (offset < 0 || offset >= this.string.length()) {
			throw new IllegalArgumentException("Invalid offset: " + offset);
		}
		this.offset = (int) offset;
	}

	@Override
	public int read() throws IOException {

		char highSurrogate = 0;

		for (;;) {
			if (this.offset >= this.string.length()) {
				return -1;
			}

			final char c = this.string.charAt(this.offset++);

			if (isHighSurrogate(c)) {
				highSurrogate = c;
				continue;
			}
			if (isLowSurrogate(c)) {
				if (highSurrogate == 0) {
					continue;
				}
				return toCodePoint(highSurrogate, c);
			}

			return c;
		}
	}

	@Override
	public void close() throws IOException {
	}

}
