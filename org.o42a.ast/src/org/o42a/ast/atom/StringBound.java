/*
    Abstract Syntax Tree
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.ast.atom;


public enum StringBound implements SignType {

	SINGLE_QUOTE("'"),
	DOUBLE_QUOTE("\""),
	SINGLE_QUOTED_LINE("'''"),
	DOUBLE_QUOTED_LINE("\"\"\"");

	private final String sign;

	StringBound(String sign) {
		this.sign = sign;
	}

	public final boolean isDoubleQuoted() {
		return (ordinal() & 1) == 1;
	}

	public final boolean isBlockBound() {
		return ordinal() >= SINGLE_QUOTED_LINE.ordinal();
	}

	@Override
	public final String getSign() {
		return this.sign;
	}

}
