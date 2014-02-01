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


public enum IDSeparator {

	NONE("", "", "") {

		@Override
		boolean discardsPrev(IDSeparator prev) {
			return false;
		}

		@Override
		boolean discardsNext(IDSeparator next) {
			return false;
		}

	},

	TOP("$$", "//", "") {

		@Override
		boolean discardsPrev(IDSeparator prev) {
			return prev != TYPE && prev != IN;
		}

	},

	SUB(":", ": ", "") {

		@Override
		boolean discardsNext(IDSeparator next) {
			return next.isNone() || next == this;
		}

	},
	ANONYMOUS(":", ": (", ")"),
	DETAIL("~~", " ~~ ", ""),
	TYPE("@@", " @@(", ")"),
	IN("@", " @(", ")");

	private final String defaultSign;
	private final String displayBefore;
	private final String displayAfter;

	IDSeparator(
			String defaultSign,
			String displayBefore,
			String displayAfter) {
		this.defaultSign = defaultSign;
		this.displayBefore = displayBefore;
		this.displayAfter = displayAfter;
	}

	public final boolean isNone() {
		return this == NONE;
	}

	public final boolean isTop() {
		return this == TOP;
	}

	public final String getDefaultSign() {
		return this.defaultSign;
	}

	public final String getDisplayBefore() {
		return this.displayBefore;
	}

	public final String getDisplayAfter() {
		return this.displayAfter;
	}

	boolean discardsPrev(IDSeparator prev) {
		return prev.isNone() || prev == this || prev == SUB;
	}

	boolean discardsNext(IDSeparator next) {
		return next == this || !next.isTop();
	}

}
