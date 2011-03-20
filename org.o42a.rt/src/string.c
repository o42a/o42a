/*
    Run-Time Library
    Copyright (C) 2010,2011 Ruslan Lopatin

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
#include "o42a/string.h"


inline size_t o42a_str_len(O42A_PARAMS const o42a_val_t *const val) {
	return val->length >> o42a_val_ashift(O42A_ARGS val);
}

inline UChar32 o42a_str_cmask(O42A_PARAMS const o42a_val_t *const val) {

	const size_t char_size = o42a_val_alignment(O42A_ARGS val);
	size_t mask;

	if (sizeof (UChar32) <= char_size) {
		// UChar32 size is less than char size. Truncate silently.
		return -1; // all ones
	}

	// UChar32 size is greater or equal to char size. Build char mask.
	return ~(-1 << (char_size << 3));
}
