/*
    Utilities
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import java.util.Arrays;


public final class Place {

	public static final Place FIRST_PLACE = new Place();

	public static Trace newTrace() {
		return FIRST_PLACE.nestedTrace();
	}

	private final int[] entries;

	private Place() {
		this.entries = new int[] {0};
	}

	private Place(int[] entries) {
		this.entries = entries;
	}

	public final int getSerial() {
		return this.entries[this.entries.length - 1];
	}

	public final int[] getEntries() {
		return this.entries;
	}

	public boolean visibleBy(Place viewer) {
		if (this == viewer) {
			// can't see itself
			return false;
		}

		final int thisLen = this.entries.length;
		final int viewerLen = viewer.entries.length;
		final int minLen;
		final boolean viewerLonger;

		if (thisLen < viewerLen) {
			minLen = thisLen;
			viewerLonger = true;
		} else {
			minLen = viewerLen;
			viewerLonger = false;
		}

		for (int i = 0; i < minLen; ++i) {

			final int e = this.entries[i];
			final int v = viewer.entries[i];

			if (v != e) {
				return v > e;
			}
		}

		return viewerLonger;
	}

	public final Trace nestedTrace() {
		return new Trace(this);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('<');
		for (int i = 0; i < this.entries.length; ++i) {
			if (i > 0) {
				out.append('.');
			}
			out.append(this.entries[i]);
		}
		out.append('>');

		return out.toString();
	}

	@Override
	public int hashCode() {
		return getSerial();
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

		final Place other = (Place) obj;

		return getSerial() == other.getSerial();
	}

	private final Place nested() {

		final int len = this.entries.length;
		final int[] newEntries = Arrays.copyOf(this.entries, len + 1);

		newEntries[len] = 0;

		return new Place(newEntries);
	}

	private Place next() {

		final int len = this.entries.length;
		final int newEntries[] = new int[len];
		final int last = len - 1;

		arraycopy(this.entries, 0, newEntries, 0, last);
		newEntries[last] = this.entries[last] + 1;

		return new Place(newEntries);
	}

	public static final class Trace {

		private final Place prefix;
		private Place next;

		private Trace(Place prefix) {
			this.prefix = prefix;
			this.next = prefix.nested();
		}

		public final Place next() {

			final Place result = this.next;

			this.next = result.next();

			return result;
		}

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();

			out.append("Trace[").append(this.prefix);
			out.append("@").append(this.next.getSerial());
			out.append(']');

			return out.toString();
		}

	}

}
