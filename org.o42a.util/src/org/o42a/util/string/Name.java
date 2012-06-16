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
package org.o42a.util.string;

import static java.lang.Character.*;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;
import static org.o42a.util.string.Capitalization.PRESERVE_CAPITALS;
import static org.o42a.util.string.Characters.HYPHEN;
import static org.o42a.util.string.Characters.NON_BREAKING_HYPHEN;


public final class Name implements CharSequence, Comparable<Name> {

	public static final Name caseInsensitiveName(String string) {
		return newName(string, null);
	}

	public static final Name caseSensitiveName(String string) {
		return newName(string, CASE_SENSITIVE);
	}

	public static Name newName(String string, Capitalization capitalization) {

		boolean preserveCapitals = capitalization != null;
		final StringBuilder out = new StringBuilder(string.length());
		final int len = string.length();
		boolean prevDigit = false;
		boolean prevLetter = false;
		boolean prevHyphen = false;
		boolean prevSeparator = false;
		boolean firstWord = true;
		int i = 0;
		boolean valid = true;

		while (i < len) {

			final int c = string.codePointAt(i);

			i += charCount(c);

			final boolean separator;

			if (Character.getType(c) == SPACE_SEPARATOR || c == '_') {
				separator = true;
			} else if (isWhitespace(c) || isISOControl(c)) {
				separator = true;
				valid = false;
			} else {
				separator = false;
			}

			if (separator) {
				if (prevSeparator) {
					continue;
				}
				if (out.length() == 0) {
					continue;
				}
				firstWord = false;
				prevSeparator = true;
				continue;
			}
			if (isDigit(c)) {
				if (out.length() == 0) {
					// Name begins with digit.
					valid = false;
				}
				if (prevSeparator) {
					if (prevDigit) {
						out.append(' ');
					}
					prevSeparator = false;
				}
				firstWord = false;
				prevDigit = true;
				prevLetter = false;
				prevHyphen = false;
				out.appendCodePoint(c);
				continue;
			}
			if (isLetter(c)) {
				if (prevSeparator) {
					if (prevLetter) {
						out.append(' ');
					}
					prevSeparator = false;
				}
				if (!preserveCapitals) {
					if (firstWord) {
						if (prevLetter && isUpperCase(c)) {
							// Abbreviation.
							preserveCapitals = true;
						}
					} else {
						if (!prevLetter && isUpperCase(c)) {
							// Proper noun.
							preserveCapitals = true;
						}
					}
				}
				prevDigit = false;
				prevLetter = true;
				prevHyphen = false;
				out.appendCodePoint(c);
				continue;
			}
			if (c == '-' || c == HYPHEN || c == NON_BREAKING_HYPHEN) {
				if (prevHyphen) {
					// Double hyphen.
					valid = false;
					continue;
				}
				if (out.length() == 0) {
					// Name begins with hyphen.
					valid = false;
				}
				if (prevSeparator) {
					// Separator before hyphen.
					valid = false;
				}
				firstWord = false;
				prevDigit = false;
				prevLetter = false;
				prevHyphen = true;
				prevSeparator = false;
				out.appendCodePoint('-');
				continue;
			}
			valid = false;
			prevDigit = false;
			prevLetter = false;
			prevHyphen = false;
			prevSeparator = false;
			out.appendCodePoint(c);
		}

		final Capitalization cap =
				capitalization != null ? capitalization
				: (preserveCapitals ? PRESERVE_CAPITALS : CASE_INSENSITIVE);

		return new Name(
				cap,
				out.toString(),
				valid && !prevHyphen && !prevSeparator,
				cap.isCaseSensitive());
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
		if (isCanonical()) {
			return new Name(
					capitalization(),
					toString().substring(start, end),
					isValid(),
					true);
		}
		if (start == 0) {
			if (end == length()) {
				return this;
			}
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

	public final String toUnderscoredString() {

		final StringNameWriter out = new StringNameWriter();

		out.underscored().canonical().write(this);

		return out.toString();
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
		if (obj.getClass() != getClass()) {
			return false;
		}

		final Name other = (Name) obj;

		return nameIs(other);
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
