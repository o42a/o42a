/*
    Abstract Syntax Tree
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
package org.o42a.ast.atom;

import org.o42a.util.string.Characters;


public enum Radix implements SignType {

	DECIMAL_RADIX(10, "") {

		@Override
		public boolean isDigit(int c) {
			return Characters.isDigit(c);
		}

	},

	HEXADECIMAL_RADIX(16, "0x") {

		@Override
		public boolean isDigit(int c) {
			return Characters.isHexDigit(c);
		}

	},

	BINARY_RADIX(2, "0b") {

		@Override
		public boolean isDigit(int c) {
			return c == '0' || c == '1';
		}

	};

	private final int radix;
	private final String sign;

	Radix(int radix, String sign) {
		this.radix = radix;
		this.sign = sign;
	}

	public final int getRadix() {
		return this.radix;
	}

	public abstract boolean isDigit(int c);

	@Override
	public final String getSign() {
		return this.sign;
	}

}
