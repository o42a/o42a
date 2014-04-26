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

import static java.lang.Character.charCount;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;


public final class Name implements SubID, CharSequence, Comparable<Name> {

	/**
	 * Creates a new case-sensitive name.
	 *
	 * <p>This method calls {@link #newName(String, Capitalization)} with
	 * capitalization set to {@link Capitalization#CASE_SENSITIVE}.</p>
	 *
	 * @param string name string.
	 *
	 * @return new name, possibly invalid.
	 */
	public static final Name caseInsensitiveName(String string) {
		return newName(string, null);
	}

	/**
	 * Creates a new case-insensitive name.
	 *
	 * <p>The exact capitalization will be determined automatically.
	 * This method calls {@link #newName(String, Capitalization)} with
	 * capitalization set to <code>null</code>.</p>
	 *
	 * @param string name string.
	 *
	 * @return new name, possibly invalid.
	 */
	public static final Name caseSensitiveName(String string) {
		return newName(string, CASE_SENSITIVE);
	}

	/**
	 * Constructs a new name.
	 *
	 * @param string name string.
	 * @param capitalization name capitalization or <code>null</code>
	 * to determine it automatically.
	 *
	 * @return new name, possibly invalid.
	 *
	 * @see NameBuilder for name construction details.
	 */
	public static Name newName(String string, Capitalization capitalization) {

		final NameBuilder out =
				new NameBuilder(capitalization, string.length());

		return out.append(string).toName();
	}

	private final Capitalization capitalization;
	private final String string;
	private int hash;
	private final boolean valid;
	private final boolean canonical;

	Name(
			Capitalization capitalization,
			String string,
			boolean valid,
			boolean canonical) {
		this.capitalization = capitalization;
		this.string = string;
		this.valid = valid;
		this.canonical = canonical;
	}

	public final Capitalization capitalization() {
		return this.capitalization;
	}

	public final boolean isValid() {
		return this.valid;
	}

	public final boolean isCanonical() {
		return this.canonical;
	}

	public final boolean isEmpty() {
		return length() == 0;
	}

	@Override
	public final int length() {
		return this.string.length();
	}

	@Override
	public final char charAt(int index) {
		return this.string.charAt(index);
	}

	@Override
	public final Name subSequence(int start, int end) {
		if (start == 0 && end == length()) {
			return this;
		}
		if (isCanonical()) {
			return new Name(
					capitalization(),
					toString().substring(start, end),
					isValid(),
					true);
		}
		if (start == 0) {
			return new Name(
					capitalization(),
					toString().substring(0, end),
					isValid(),
					isCanonical());
		}
		return caseInsensitiveName(toString().substring(start, end));
	}

	public final int codePointAt(int index) {
		return this.string.codePointAt(index);
	}

	public final boolean is(Name other) {
		if (other == null) {
			return false;
		}
		return nameIs(other);
	}

	@Override
	public int compareTo(Name o) {
		if (isCanonical() && o.isCanonical()) {
			return toString().compareTo(o.toString());
		}

		final Capitalization cap1 = capitalization();
		final Capitalization cap2 = o.capitalization();
		final int len1 = length();
		final int len2 = o.length();

		int i1 = 0;
		int i2 = 0;

		while (i1 < len1 && i2 < len2) {

			int c1 = cap1.canonical(codePointAt(i1));
			int c2 = cap2.canonical(o.codePointAt(i2));

			if (c1 != c2) {
				return c1 - c2;
			}

			c1 += charCount(c1);
			c2 += charCount(c2);
		}

		return len1 - len2;
	}

	@Override
	public int hashCode() {

		final int len = length();

		if (len == 0) {
			return 0;
		}

		int hash = this.hash;

		if (hash != 0) {
			return hash;
		}

		int i = 0;
		final Capitalization cap = capitalization();

		do {

			final int cp = cap.canonical(codePointAt(i));

			hash = 31 * hash + cp;
			i += charCount(cp);
		} while (i < len);

		return this.hash = hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}

		final Name other = (Name) obj;

		return nameIs(other);
	}

	@Override
	public final ID toID() {
		return new ID(null, IDSeparator.NONE, this, null);
	}

	@Override
	public ID toDisplayID() {
		return toID();
	}

	@Override
	public final String toString() {
		return this.string;
	}

	private final boolean nameIs(Name o) {
		if (isCanonical() && o.isCanonical()) {
			return toString().equals(o.toString());
		}

		final Capitalization cap1 = capitalization();
		final Capitalization cap2 = o.capitalization();
		final int len1 = length();
		final int len2 = o.length();

		int i1 = 0;
		int i2 = 0;

		while (i1 < len1 && i2 < len2) {

			int c1 = cap1.canonical(codePointAt(i1));
			int c2 = cap2.canonical(o.codePointAt(i2));

			if (c1 != c2) {
				return false;
			}

			i1 += charCount(c1);
			i2 += charCount(c2);
		}

		return len1 == len2;
	}

}
