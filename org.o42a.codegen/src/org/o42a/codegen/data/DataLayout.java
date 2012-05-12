/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.codegen.data;

import org.o42a.util.DataAlignment;


public final class DataLayout {

	private static final int ALIGNMENT_MASK = 0xE0000000;
	private static final int SIZE_MASK = 0x1FFFFFFF;

	private final int layout;

	public DataLayout(int size, DataAlignment alignment) {
		this.layout = size | (alignment.getShift() << 29);
	}

	public DataLayout(int binaryForm) {
		this.layout = binaryForm;
	}

	public final DataAlignment getAlignment() {
		return DataAlignment.alignmentByShift(getAlignmentShift());
	}

	public final int getSize() {
		return this.layout & SIZE_MASK;
	}

	public final byte getAlignmentShift() {
		return (byte) ((this.layout & ALIGNMENT_MASK) >>> 29);
	}

	public final int toBinaryForm() {
		return this.layout;
	}

	@Override
	public int hashCode() {
		return this.layout;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final DataLayout other = (DataLayout) obj;

		return this.layout == other.layout;
	}

	@Override
	public String toString() {
		return "DataLayout[size=" + getSize()
				+ ", alignment=" + getAlignment() + ']';
	}

}
