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


public class StringCPWriter extends CPWriter {

	private StringBuilder out;

	public StringCPWriter() {
	}

	public StringCPWriter(StringBuilder out) {
		this.out = out;
	}

	@Override
	public void expandCapacity(int size) {
		if (this.out == null) {
			this.out = new StringBuilder(size);
		} else {
			this.out.ensureCapacity(this.out.length() + size);
		}
	}

	public final StringBuilder out() {
		if (this.out != null) {
			return this.out;
		}
		return this.out = new StringBuilder();
	}

	@Override
	public String toString() {
		if (this.out == null) {
			return "";
		}
		return this.out.toString();
	}

	@Override
	public void writeCodePoint(int codePoint) {
		out().appendCodePoint(codePoint);
	}

}
