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

import static java.lang.Character.toLowerCase;


public enum Capitalization {

	/**
	 * Name is case insensitive.
	 *
	 * <p>The first letter can be de-capitalized when the name is displayed.</p>
	 */
	CASE_INSENSITIVE() {

		@Override
		public int decapitalizeFirst(int firstCodePoint) {
			return toLowerCase(firstCodePoint);
		}

	},

	/**
	 * Name is case-insensitive, but when the name is displayed the first
	 * capital letter is preserved.
	 *
	 * <p>This is useful for abbreviations like "URL" and proper nouns like
	 * "John Smith".</p>
	 */
	PRESERVE_CAPITAL,

	/**
	 * Name is case sensitive.
	 */
	CASE_SENSITIVE() {

		@Override
		public int canonical(int codePoint) {
			return codePoint;
		}

	},

	/**
	 * Name is case sensitive and is always represented as is.
	 *
	 * <p>It is expected that such name contains only ASCII characters.</p>
	 */
	AS_IS() {

		@Override
		public int canonical(int codePoint) {
			return codePoint;
		}

	};

	public final boolean preservesCapital() {
		return ordinal() >= PRESERVE_CAPITAL.ordinal();
	}

	public final boolean isCaseSensitive() {
		return ordinal() >= CASE_SENSITIVE.ordinal();
	}

	public final boolean isRaw() {
		return this == AS_IS;
	}

	public int decapitalizeFirst(int firstCodePoint) {
		return firstCodePoint;
	}

	public int canonical(int codePoint) {
		return Character.toLowerCase(codePoint);
	}

	public final Name name(String name) {
		return new Name(this, name, true, isCaseSensitive());
	}

	public final Name canonicalName(String name) {
		return new Name(this, name, true, true);
	}

}
