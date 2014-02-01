/*
    Abstract Syntax Tree
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
package org.o42a.ast.file;

import java.util.Arrays;

import org.o42a.ast.atom.SignType;


public abstract class LineType implements SignType {

	private final char lineChar;
	private final int length;

	public LineType(char lineChar, int length) {
		this.lineChar = lineChar;
		this.length = length;
	}

	public final char getLineChar() {
		return this.lineChar;
	}

	public final int getLength() {
		return this.length;
	}

	@Override
	public String getSign() {

		final char[] chars = new char[this.length];

		Arrays.fill(chars, this.lineChar);

		return new String(chars);
	}

	@Override
	public String toString() {
		return new String(
				new char[] {this.lineChar, this.lineChar, this.lineChar});
	}

}
