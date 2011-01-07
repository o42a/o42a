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

#include "o42a/debug.h"


inline size_t o42a_str_len(const o42a_val_t *const val) {
	return val->length >> o42a_val_ashift(val);
}

inline wchar_t o42a_str_wchar_mask(const o42a_val_t *const val) {

	const size_t char_size = o42a_val_alignment(val);
	size_t mask;

	if (sizeof (wchar_t) <= char_size) {
		// wchar_t size is less than char size - truncate silently
		return -1; // all ones
	}

	// wchar_t size is greater or equal to char size
	// build wchar mask
	return ~(-1 << (char_size << 3));
}

void o42a_str_get(wchar_t *const dest, const o42a_val_t *const val) {
	O42A_ENTER;

	const size_t len = o42a_str_len(val);

	if (!len) {
		return;
	}

	const size_t step = o42a_val_alignment(val);
	const size_t mask = o42a_str_wchar_mask(val);
	const void *const src = o42a_val_data(val);

	for (size_t s = 0, d = 0; d < len; s += step, ++d) {
		dest[d] = *((wchar_t*) (src + s)) & mask;
	}

	O42A_RETURN;
}
