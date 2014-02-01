/*
    Compiler Commons
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
package org.o42a.common.phrase.part;


/**
 * Indicates whether all the preceding phrase part should form a phrase,
 * which, in turn, should be interpreted as a prefix of a phrase. The new phrase
 * will consist of the rest of the parts.
 */
public enum PartsAsPrefix {

	/** Parts does not form a new phrase prefix. This is the default. */
	NOT_PREFIX,

	/** Parts, including the last processed one, form a new phrase prefix. */
	PREFIX_WITH_LAST,

	/**
	 * Parts, excluding the last processed one, form a new phrase prefix.
	 *
	 * The last part will be the first one of the new phrase.
	 */
	PREFIX_WITHOUT_LAST,

	/**
	 * Parts, ignoring the last processed one, form a new phrase prefix.
	 *
	 * The last part will be ignored in the new phrase.
	 */
	PREFIX_IGNORE_LAST;

	/**
	 * Whether parts form a new phrase prefix.
	 *
	 * @return <code>true</code> if so, or <code>false</code> otherwise.
	 */
	public final boolean isPrefix() {
		return this != NOT_PREFIX;
	}

	/**
	 * Whether the last processed part should be included in current phrase.
	 *
	 * @return <code>true</code> if so, or <code>false</code> otherwise.
	 */
	public final boolean includeLast() {
		return ordinal() <= PREFIX_WITH_LAST.ordinal();
	}

	/**
	 * Whether a new phrase starts with the last processed part.
	 *
	 * @return <code>true</code> if so, or <code>false</code> to start with the
	 * part following the last processed one.
	 */
	public final boolean startNewPhraseFromLast() {
		return this == PREFIX_WITHOUT_LAST;
	}

}
