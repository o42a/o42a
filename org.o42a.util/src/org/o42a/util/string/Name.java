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


public final class Name implements Comparable<Name> {

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

	public final int length() {
		return this.string.length();
	}

	public final char charAt(int index) {
		return this.string.charAt(index);
	}

	public final int codePointAt(int index) {
		return this.string.codePointAt(index);
	}

	public Name decapitalize() {

		final int length = length();

		if (length < 2) {
			if (length == 0) {
				return this;
			}

			final int oldFirst = charAt(0);
			final int newFirst = capitalization().decapitalizeFirst(oldFirst);

			if (oldFirst == newFirst) {
				return this;
			}

			return new Name(
					capitalization(),
					new String(toChars(newFirst)),
					isValid(),
					true);
		}

		final int oldFirst;
		final int oldFirstLen;
		final char c1 = charAt(0);
		final char c2 = charAt(1);

		if (isSurrogatePair(c1, c2)) {
			oldFirst = toCodePoint(c1, c2);
			oldFirstLen = 2;
		} else {
			oldFirst = c1;
			oldFirstLen = 1;
		}

		final int newFirst = capitalization().decapitalizeFirst(oldFirst);

		if (oldFirst == newFirst) {
			return this;
		}

		final int restLen = length() - oldFirstLen;
		final char[] newChars;
		final int newFirstLen;

		if (newFirst < MIN_SUPPLEMENTARY_CODE_POINT) {
			newFirstLen = 1;
			newChars = new char[newFirstLen + restLen];
			newChars[0] = (char) newFirst;
		} else {
			newFirstLen = 2;
			newChars = new char[newFirstLen + restLen];

			final int offset = newFirst - MIN_SUPPLEMENTARY_CODE_POINT;

			newChars[1] = (char) ((offset & 0x3ff) + MIN_LOW_SURROGATE);
	        newChars[0] = (char) ((offset >>> 10) + MIN_HIGH_SURROGATE);
		}

		this.string.getChars(oldFirstLen, length(), newChars, newFirstLen);

		return new Name(
				capitalization(),
				new String(newChars),
				isValid(),
				false);
	}

	public final Name toCanonocal() {
		if (isCanonical()) {
			return this;
		}
		return capitalization().canonicalName(this);
	}

	public final String toUnderscopedString() {

		final String string = toCanonocal().toString();
		final int length = string.length();
		StringBuilder out = null;

		for (int i = 0; i < length; ++i) {

			final char c = string.charAt(i);

			if (c == ' ') {
				if (out == null) {
					out = new StringBuilder(length);
					out.append(string, 0, i);
				}
				out.append('_');
			} else if (out != null) {
				out.append(c);
			}
		}

		if (out == null) {
			return string;
		}

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
		if (capitalization().isCaseSensitive()) {
			if (o.capitalization().isCaseSensitive()) {
				return toString().compareTo(o.toString());
			}
		} else {
			if (!o.capitalization().isCaseSensitive()) {
				return toString().compareToIgnoreCase(o.toString());
			}
		}
		return toCanonocal().toString().compareTo(o.toCanonocal().toString());
	}

	@Override
	public int hashCode() {
		return toCanonocal().toString().hashCode();
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

	private final boolean nameIs(Name other) {
		if (capitalization().isCaseSensitive()) {
			if (other.capitalization().isCaseSensitive()) {
				return toString().equals(other.toString());
			}
		} else {
			if (!other.capitalization().isCaseSensitive()) {
				return toString().equalsIgnoreCase(other.toString());
			}
		}
		return toCanonocal().toString().equals(other.toCanonocal().toString());
	}

}
