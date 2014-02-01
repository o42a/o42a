/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import org.o42a.util.DataAlignment;


/**
 * Type data alignment.
 *
 * <p>This indicates the minimum alignment {@link Type#requiredAlignment()
 * required} by data type. This also indicates if data type is packed.</p>
 */
public enum TypeAlignment {

	/**
	 * Data type is packed, i.e. always 1-byte aligned.
	 *
	 * <p>The packed data type may only contain scalar values, pointers
	 * or other packed structures. The atomic operations on packed data are not
	 * necessarily supported.</p>
	 */
	PACKED_TYPE(DataAlignment.ALIGN_1),

	/**
	 * At least 1 byte data alignment.
	 *
	 * <p>This is the default type data alignment value, as any data is
	 * 1-byte aligned.</p>
	 */
	TYPE_ALIGN_1(DataAlignment.ALIGN_1),

	/** At least 2 bytes data alignment. */
	TYPE_ALIGN_2(DataAlignment.ALIGN_2),

	/** At least 4 bytes data alignment. */
	TYPE_ALIGN_4(DataAlignment.ALIGN_4),

	/** At least 8 bytes data alignment. */
	TYPE_ALIGN_8(DataAlignment.ALIGN_8),

	/** At least 16 bytes data alignment. */
	TYPE_ALIGN_16(DataAlignment.ALIGN_16),

	/** At least 32 bytes data alignment. */
	TYPE_ALIGN_32(DataAlignment.ALIGN_32),

	/** At least 64 bytes data alignment. */
	TYPE_ALIGN_64(DataAlignment.ALIGN_64),

	/** At least 128 bytes data alignment. */
	TYPE_ALIGN_128(DataAlignment.ALIGN_128);

	private final DataAlignment alignment;

	TypeAlignment(DataAlignment alignment) {
		this.alignment = alignment;
	}

	/**
	 * Whether the type data is packed.
	 *
	 * @return <code>true</code> if type is {@link #PACKED_TYPE packed}, or
	 * <code>false</code> otherwise.
	 */
	public final boolean isPacked() {
		return this == PACKED_TYPE;
	}

	/**
	 * The data alignment corresponding to this type alignment.
	 *
	 * @return data alignment.
	 */
	public final DataAlignment getAlignment() {
		return this.alignment;
	}

}
