/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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


public final class DataLayout {

	private static final int ALIGNMENT_MASK = 0xE0000000;
	private static final int SIZE_MASK = 0x1FFFFFFF;

	public static byte alignmentShift(short alignment) {

		byte shift;
		final int diff = alignment - 4;

		if (diff <= 0) {
			if (diff == 0) {
				shift = 2;
			} else if (alignment == 2) {
				shift = 1;
			} else {
				throw new IllegalStateException(
						"Wrong alignment: " + alignment);
			}
		} else {
			switch (alignment) {
			case 8: shift = 3; break;
			case 16: shift = 4; break;
			case 32: shift = 5; break;
			case 64: shift = 6; break;
			case 128: shift = 7; break;
			default:
				throw new IllegalStateException(
						"Wrong alignment: " + alignment);
			}
		}

		return shift;
	}

	private final int layout;

	public DataLayout(int size, short alignment) {
		this.layout = size | (alignmentShift(alignment) << 29);
	}

	public DataLayout(int binaryForm) {
		this.layout = binaryForm;
	}

	public final short getAlignment() {
		return (short) (1 << getAlignmentShift());
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
	public String toString() {
		return "DataLayout[size=" + getSize()
		+ ", alignment=" + getAlignment() + ']';
	}

}
