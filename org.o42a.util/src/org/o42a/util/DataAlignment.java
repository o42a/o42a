/*
    Utilities
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
package org.o42a.util;

import static java.lang.Integer.numberOfLeadingZeros;


public enum DataAlignment {

	ALIGN_1(1, 0),
	ALIGN_2(2, 1),
	ALIGN_4(4, 2),
	ALIGN_8(8, 3),
	ALIGN_16(16, 4),
	ALIGN_32(32, 5),
	ALIGN_64(64, 6),
	ALIGN_128(128, 7);

	public static DataAlignment alignmentByShift(int shift) {
		return Registry.byShift[shift];
	}

	public static DataAlignment alignmentOfBytes(int bytes) {

		final int diff = bytes - 4;

		if (diff <= 0) {
			if (diff == 0) {
				return ALIGN_4;
			}
			if (bytes == 2) {
				return ALIGN_2;
			}
			return ALIGN_1;
		}

		switch (bytes) {
		case 8: return ALIGN_8;
		case 16: return ALIGN_16;
		case 32: return ALIGN_32;
		case 64: return ALIGN_64;
		case 128: return ALIGN_128;
		default:
			throw new IllegalStateException(
					"Wrong alignment: " + bytes);
		}
	}

	public static DataAlignment maxAlignmentBelowSize(int bytes) {
		if (bytes == 0) {
			return ALIGN_1;
		}
		return alignmentByShift(31 - numberOfLeadingZeros(bytes));
	}

	private final short bytes;
	private final byte shift;

	DataAlignment(int bytes, int shift) {
		this.bytes = (short) bytes;
		this.shift = (byte) shift;
		Registry.byShift[shift] = this;
	}

	public final short getBytes() {
		return this.bytes;
	}

	public final byte getShift() {
		return this.shift;
	}

	public final DataAlignment union(DataAlignment other) {
		if (other.getBytes() > getBytes()) {
			return other;
		}
		return this;
	}

	@Override
	public String toString() {
		if (this.bytes == 1) {
			return "1 byte";
		}
		return this.bytes + " bytes";
	}

	private static final class Registry {

		private static final DataAlignment[] byShift = new DataAlignment[8];

	}

}
