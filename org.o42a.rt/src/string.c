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

#include <string.h>

#include "o42a/memory.h"


inline size_t o42a_str_len(O42A_PARAMS const o42a_val_t *const val) {
	return val->length >> o42a_val_ashift(O42A_ARGS_ val);
}

inline UChar32 o42a_str_cmask(O42A_PARAMS const o42a_val_t *const val) {

	const size_t char_size = o42a_val_alignment(O42A_ARGS_ val);
	size_t mask;

	if (sizeof (UChar32) <= char_size) {
		// UChar32 size is less than char size. Truncate silently.
		return -1; // all ones
	}

	// UChar32 size is greater or equal to char size. Build char mask.
	return ~(-1 << (char_size << 3));
}

void o42a_str_sub(
		O42A_PARAMS
		o42a_val_t *const sub,
		const o42a_val_t *const string,
		const int64_t from,
		const int64_t to) {
	O42A_ENTER(return);

	if (from > to || from < 0) {
		// Invalid char indices.
		sub->flags = O42A_FALSE;
		O42A_RETURN;
	}

	const size_t ashift = O42A(o42a_val_ashift(O42A_ARGS string));
	const size_t bfrom = from << ashift;
	const size_t bto = to << ashift;
	const size_t len = string->length;

	if (bto >= len) {
		// Invalid char index.
		sub->flags = O42A_FALSE;
		O42A_RETURN;
	}

	const size_t sublen = bto - bfrom;

	if (!sublen) {
		// Empty substring requested.
		sub->flags = O42A_TRUE;
		sub->length = 0;
		O42A_RETURN;
	}
	if (!bfrom && bto == len - 1) {
		// Full string requested.
		*sub = *string;
		O42A_RETURN;
	}

	const void *const str = O42A(o42a_val_data(O42A_ARGS string));
	void *substr;

	sub->length = sublen;
	if (sublen <= 8) {
		sub->flags = O42A_TRUE | (string->flags & O42A_VAL_ALIGNMENT_MASK);
		substr = &sub->value;
	} else {
		sub->flags =
				O42A_TRUE | O42A_VAL_EXTERNAL
				| (string->flags & O42A_VAL_ALIGNMENT_MASK);
		sub->value.v_ptr = substr = O42A(o42a_mem_alloc_rc(O42A_ARGS sublen));
	}

	memcpy(substr, str + bfrom, sublen);

	O42A_RETURN;
}
