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
import static org.o42a.util.string.Capitalization.PRESERVE_CAPITAL;
import static org.o42a.util.string.Characters.HYPHEN;
import static org.o42a.util.string.Characters.NON_BREAKING_HYPHEN;

import org.o42a.util.string.ID.Separator;


public final class Name implements CharSequence, Comparable<Name> {

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
	 * <p>This method removes extra word separators and validates the name.</p>
	 *
	 * <p>If {@code capitalization} is omitted, then it is considered case
	 * insensitive, i.e. either {@link Capitalization#CASE_INSENSITIVE} or
	 * {@link Capitalization#PRESERVE_CAPITAL} depending on name.</p>
	 *
	 * @param string name string.
	 * @param capitalization name capitalization or <code>null</code>
	 * to determine it automatically.
	 *
	 * @return new name, possibly invalid.
	 */
	public static Name newName(String string, Capitalization capitalization) {

		boolean preserveCapital = capitalization != null;
		final StringBuilder out = new StringBuilder(string.length());
		final int len = string.length();
		boolean prevDigit = false;
		boolean prevLetter = false;
		boolean prevHyphen = false;
		boolean prevSeparator = false;
		boolean firstWord = true;
		boolean wordCapital = false;
		boolean wordAbbr = false;
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
				if (!preserveCapital
						&& !firstWord
						&& wordCapital
						&& !wordAbbr) {
					preserveCapital = true;
				}
				firstWord = false;
				wordCapital = false;
				wordAbbr = false;
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
				if (!preserveCapital
						&& !firstWord
						&& wordCapital
						&& !wordAbbr) {
					preserveCapital = true;
				}
				firstWord = false;
				wordCapital = false;
				wordAbbr = false;
				prevDigit = true;
				prevLetter = false;
				prevHyphen = false;
				out.appendCodePoint(c);
				continue;
			}
			if (isLetter(c)) {
				if (!preserveCapital) {
					if (prevSeparator || !prevLetter) {
						if (isUpperCase(c)) {
							// Word starts with capital letter.
							// Proper noun.
							wordCapital = true;
						} else if (firstWord) {
							// First word starts with non-capital.
							// Preserve it.
							preserveCapital = true;
						}
					} else if (prevLetter && isUpperCase(c)) {
						// Abbreviation.
						if (firstWord) {
							// First word is abbreviation.
							// Preserve capital.
							preserveCapital = true;
						} else {
							wordAbbr = true;
						}
					}
				}
				if (prevSeparator) {
					if (prevLetter) {
						out.append(' ');
					}
					prevSeparator = false;
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
				if (!preserveCapital
						&& !firstWord
						&& wordCapital
						&& !wordAbbr) {
					preserveCapital = true;
				}
				firstWord = false;
				wordCapital = false;
				wordAbbr = false;
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

		final Capitalization cap;

		if (capitalization != null) {
			cap = capitalization;
		} else {
			if (!preserveCapital
					&& !firstWord
					&& wordCapital
					&& !wordAbbr) {
				preserveCapital = true;
			}
			cap = preserveCapital ? PRESERVE_CAPITAL : CASE_INSENSITIVE;
		}

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

	public final ID toID() {
		return new ID(null, Separator.NONE, this, null, false);
	}

	public final ID toRawID() {
		return new ID(null, Separator.NONE, this, null, true);
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
