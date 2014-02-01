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

import static java.lang.Character.*;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;
import static org.o42a.util.string.Capitalization.PRESERVE_CAPITAL;
import static org.o42a.util.string.Characters.HYPHEN;
import static org.o42a.util.string.Characters.NON_BREAKING_HYPHEN;


/**
 * The {@link Name name} builder.
 *
 * <p>This builder removes an extra word separators and validates the name.</p>
 *
 * <p>If {@code capitalization} is omitted from constructor, then the name is
 * considered case insensitive, i.e. either
 * {@link Capitalization#CASE_INSENSITIVE} or
 * {@link Capitalization#PRESERVE_CAPITAL} depending on name contents.</p>
 */
public class NameBuilder implements CharSequence {

	private final Capitalization capitalization;
	private boolean preserveCapital;
	private final StringBuilder out;
	private boolean prevDigit = false;
	private boolean prevLetter = false;
	private boolean prevHyphen = false;
	private boolean prevSeparator = false;
	private boolean firstWord = true;
	private boolean wordCapital = false;
	private boolean wordAbbr = false;
	private boolean valid = true;

	/**
	 * Constructs the name builder, which automatically determines
	 * the name capitalization.
	 */
	public NameBuilder() {
		this(null, new StringBuilder());
	}

	/**
	 * Constructs the name builder.
	 *
	 * @param capitalization name capitalization or <code>null</code>
	 * to determine it automatically.
	 */
	public NameBuilder(Capitalization capitalization) {
		this(capitalization, new StringBuilder());
	}

	/**
	 * Constructs the name builder with the given capacity.
	 *
	 * @param capitalization name capitalization or <code>null</code>
	 * to determine it automatically.
	 * @param capacity the initial capacity.
	 */
	public NameBuilder(Capitalization capitalization, int capacity) {
		this(capitalization, new StringBuilder(capacity));
	}

	private NameBuilder(Capitalization capitalization, StringBuilder out) {
		this.capitalization = capitalization;
		this.preserveCapital = capitalization != null;
		this.out = out;
	}

	/**
	 * Whether the constructed name is valid.
	 *
	 * @return <code>true</code> if constructed name is valid so far,
	 * or <code>false</code> otherwise.
	 */
	public final boolean isValid() {
		return this.valid;
	}

	/**
	 * Appends the given string to the name.
	 *
	 * @param string string to append.
	 *
	 * @return this name builder.
	 */
	public NameBuilder append(String string) {

		final int len = string.length();

		this.out.ensureCapacity(this.out.length() + len);

		int i = 0;

		while (i < len) {

			final int c = string.codePointAt(i);

			i += charCount(c);

			append(c);
		}

		return this;
	}

	/**
	 * Appends the given code point to the name.
	 *
	 * @param c Unicode code point to append.
	 *
	 * @return this name builder.
	 */
	public NameBuilder append(int c) {

		final boolean separator;

		if (isWordSeparator(c)) {
			separator = true;
		} else if (isInvalidWordSeparator(c)) {
			separator = true;
			this.valid = false;
		} else {
			separator = false;
		}

		if (separator) {
			if (this.prevSeparator) {
				return this;
			}
			if (this.out.length() == 0) {
				return this;
			}
			if (!this.preserveCapital
					&& !this.firstWord
					&& this.wordCapital
					&& !this.wordAbbr) {
				this.preserveCapital = true;
			}
			this.firstWord = false;
			this.wordCapital = false;
			this.wordAbbr = false;
			this.prevSeparator = true;
			return this;
		}
		if (isDigit(c)) {
			if (this.out.length() == 0) {
				// Name begins with digit.
				this.valid = false;
			}
			if (this.prevSeparator) {
				if (this.prevDigit) {
					this.out.append(' ');
				}
				this.prevSeparator = false;
			}
			if (!this.preserveCapital
					&& !this.firstWord
					&& this.wordCapital
					&& !this.wordAbbr) {
				this.preserveCapital = true;
			}
			this.firstWord = false;
			this.wordCapital = false;
			this.wordAbbr = false;
			this.prevDigit = true;
			this.prevLetter = false;
			this.prevHyphen = false;
			this.out.appendCodePoint(c);
			return this;
		}
		if (isLetter(c)) {
			if (!this.preserveCapital) {
				if (this.prevSeparator || !this.prevLetter) {
					if (isUpperCase(c)) {
						// Word starts with capital letter.
						// Proper noun.
						this.wordCapital = true;
					} else if (this.firstWord) {
						// First word starts with non-capital.
						// Preserve it.
						this.preserveCapital = true;
					}
				} else if (this.prevLetter && isUpperCase(c)) {
					// Abbreviation.
					if (this.firstWord) {
						// First word is abbreviation.
						// Preserve capital.
						this.preserveCapital = true;
					} else {
						this.wordAbbr = true;
					}
				}
			}
			if (this.prevSeparator) {
				if (this.prevLetter) {
					this.out.append(' ');
				}
				this.prevSeparator = false;
			}
			this.prevDigit = false;
			this.prevLetter = true;
			this.prevHyphen = false;
			this.out.appendCodePoint(c);
			return this;
		}
		if (c == '-' || c == HYPHEN || c == NON_BREAKING_HYPHEN) {
			if (this.prevHyphen) {
				// Double hyphen.
				this.valid = false;
				return this;
			}
			if (this.out.length() == 0) {
				// Name begins with hyphen.
				this.valid = false;
			}
			if (this.prevSeparator) {
				// Separator before hyphen.
				this.valid = false;
			}
			if (!this.preserveCapital
					&& !this.firstWord
					&& this.wordCapital
					&& !this.wordAbbr) {
				this.preserveCapital = true;
			}
			this.firstWord = false;
			this.wordCapital = false;
			this.wordAbbr = false;
			this.prevDigit = false;
			this.prevLetter = false;
			this.prevHyphen = true;
			this.prevSeparator = false;
			this.out.appendCodePoint('-');
			return this;
		}
		this.valid = false;
		this.prevDigit = false;
		this.prevLetter = false;
		this.prevHyphen = false;
		this.prevSeparator = false;
		this.out.appendCodePoint(c);

		return this;
	}

	@Override
	public final int length() {
		return this.out.length();
	}

	@Override
	public final char charAt(int index) {
		return this.out.charAt(index);
	}

	/**
     * Returns the Unicode code point at the specified index.
     *
     * @param index the index to the <code>char</code> values.
     *
     * @return the code point value of the character at the <code>index</code>.
     *
     * @see StringBuilder#codePointAt(int) for the detailed description.
	 */
	public final int codePointAt(int index) {
		return this.out.codePointAt(index);
	}

	@Override
	public final CharSequence subSequence(int start, int end) {
		return this.out.subSequence(start, end);
	}

	/**
	 * Constructs the name from data added to the builder.
	 *
	 * @return constructed name, possibly invalid.
	 */
	public Name toName() {

		final Capitalization capitalization;

		if (this.capitalization != null) {
			capitalization = this.capitalization;
		} else {
			if (!this.preserveCapital
					&& !this.firstWord
					&& this.wordCapital
					&& !this.wordAbbr) {
				this.preserveCapital = true;
			}
			capitalization =
					this.preserveCapital ? PRESERVE_CAPITAL : CASE_INSENSITIVE;
		}

		return new Name(
				capitalization,
				this.out.toString(),
				this.valid && !this.prevHyphen && !this.prevSeparator,
				capitalization.isCaseSensitive());
	}

	@Override
	public String toString() {
		return this.out.toString();
	}

	protected boolean isWordSeparator(int c) {
		return Character.getType(c) == SPACE_SEPARATOR || c == '_';
	}

	protected boolean isInvalidWordSeparator(int c) {
		return isWhitespace(c) || isISOControl(c);
	}

}
