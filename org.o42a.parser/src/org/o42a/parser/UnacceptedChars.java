/*
    Parser
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
package org.o42a.parser;

import java.util.Arrays;


final class UnacceptedChars {

	private long[] offsets = new long[16];
	private int[] codePoints = new int[16];
	private int length;

	public final int get(int index) {
		assert index < this.length :
			"Invalid code point index";
		return this.codePoints[index];
	}

	public final long offset(int index) {
		assert index < this.length :
			"Invalid code point index";
		return this.offsets[index];
	}

	public final long length() {
		return this.length;
	}

	public final void append(long offset, int cp) {
		if (this.length >= this.offsets.length) {

			final int capacity = this.length + (this.length >> 1);

			this.offsets = Arrays.copyOf(this.offsets, capacity);
			this.codePoints = Arrays.copyOf(this.codePoints, capacity);
		}

		this.offsets[this.length] = offset;
		this.codePoints[this.length] = cp;
		++this.length;
	}

	public void accept(int accept) {
		this.length -= accept;
		System.arraycopy(
				this.offsets,
				accept,
				this.offsets,
				0,
				this.length);
		System.arraycopy(
				this.codePoints,
				accept,
				this.codePoints,
				0,
				this.length);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder(this.length);

		out.append('"');
		for (int i = 0; i < this.length; ++i) {
			out.appendCodePoint(this.codePoints[i]);
		}
		out.append('"');

		return out.toString();
	}

}
