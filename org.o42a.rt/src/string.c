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

	if (bto > len) {
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
	if (!bfrom && bto == len) {
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

	O42A(memcpy(substr, str + bfrom, sublen));

	O42A_RETURN;
}

int64_t o42a_str_compare(
		O42A_PARAMS
		const o42a_val_t *const what,
		const o42a_val_t *const with) {
	O42A_ENTER(return 0);

	const void *str1 = O42A(o42a_val_data(O42A_ARGS what));
	const UChar32 cmask1 = O42A(o42a_str_cmask(O42A_ARGS what));
	const size_t step1 = O42A(o42a_val_alignment(O42A_ARGS what));
	const size_t len1 = what->length;
	const void *const end1 = str1 + len1;

	const void *str2 = O42A(o42a_val_data(O42A_ARGS with));
	const UChar32 cmask2 = O42A(o42a_str_cmask(O42A_ARGS with));
	const size_t step2 = O42A(o42a_val_alignment(O42A_ARGS with));
	const size_t len2 = with->length;
	const void *const end2 = str2 + len2;

	while (str1 < end1 && str2 < end2) {

		const UChar32 c1 = cmask1 & *((UChar32*) str1);
		const UChar32 c2 = cmask1 & *((UChar32*) str2);

		if (c1 < c2) {
			O42A_RETURN -1;
		}
		if (c1 > c2) {
			O42A_RETURN 1;
		}

		str1 += step1;
		str2 += step2;
	}

	if (len1 == len2) {
		O42A_RETURN 0;
	}
	if (len1 < len2) {
		O42A_RETURN -1;
	}

	O42A_RETURN 1;
}

void o42a_str_concat(
		O42A_PARAMS
		o42a_val_t *result,
		const o42a_val_t *str1,
		const o42a_val_t *str2) {
	O42A_ENTER(return);

	const size_t ashift1 = O42A(o42a_val_ashift(O42A_ARGS str1));
	const size_t len1 = str1->length;
	const void *data1 = O42A(o42a_val_data(O42A_ARGS str1));

	const size_t ashift2 = O42A(o42a_val_ashift(O42A_ARGS str1));
	const size_t len2 = str2->length;
	const void *data2 = O42A(o42a_val_data(O42A_ARGS str2));

	size_t ashift;
	size_t len;

	if (ashift1 >= ashift2) {
		ashift = ashift1;
		len = len1 + ((len2 >> ashift2) << ashift1);
	} else {
		ashift = ashift2;
		len = len2 + ((len1 >> ashift1) << ashift2);
	}

	void *data;

	if (len <= 8) {
		result->flags = O42A_TRUE;
		data = &result->value;
	} else {
		result->flags = O42A_TRUE | O42A_VAL_EXTERNAL | (ashift << 8);
		data = O42A(o42a_mem_alloc_rc(O42A_ARGS len));
	}

	int8_t* copy_to;
	const int8_t* copy_from;
	const int8_t* copy_from_end;
	size_t copy_bytes;

	if (ashift1 >= ashift2) {
		O42A(memcpy(data, data1, len1));
		if (ashift1 == ashift2) {
			O42A(memcpy(data + len1, str2, len2));
			O42A_RETURN;
		}
		copy_to = (int8_t*) data + len1;
		copy_from = (int8_t*) str2;
		copy_from_end = copy_from + len2;
		copy_bytes = 1 << ashift2;
	} else {
		O42A(memcpy(data + len - len2, str2, len2));
		copy_to = (int8_t*) data;
		copy_from = (int8_t*) data1;
		copy_from_end = copy_from + len1;
		copy_bytes = 1 << ashift1;
	}

	const size_t skip_bytes = (1 << ashift) - copy_bytes;

	while (copy_from < copy_from_end) {
		for (size_t i = 0; i < copy_bytes; ++i) {
			*(copy_to++) = *(copy_from++);
		}
		for (size_t i = 0; i < skip_bytes; ++i) {
			*(copy_to++) = 0;
		}
	}

	O42A_RETURN;
}
