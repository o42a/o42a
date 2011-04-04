/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.def;


public enum DefKind {

	REQUIREMENT(true, false),
	CONDITION(false, false),
	CLAIM(true, true),
	PROPOSITION(false, true);

	private final boolean claim;
	private final boolean value;

	DefKind(boolean claim, boolean value) {
		this.claim = claim;
		this.value = value;
	}

	public final boolean isClaim() {
		return this.claim;
	}

	public final boolean isValue() {
		return this.value;
	}

	public final DefKind claim() {
		return isValue() ? CLAIM : REQUIREMENT;
	}

	public final DefKind unclaim() {
		return isValue() ? PROPOSITION : CONDITION;
	}

}
