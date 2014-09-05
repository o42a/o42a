/*
    Copyright (C) 2011-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/type/integer.h"

#include <stdio.h>
#include <string.h>

#include "o42a/error.h"
#include "o42a/memory/refcount.h"
#include "o42a/type/string.h"

#include "unicode/uchar.h"

/**
 * Float value type descriptor.
 */
const o42a_val_type_t o42a_val_type_integer = O42A_VAL_TYPE(
		"integer",
		o42a_val_copy_as_is,
		o42a_val_gc_none,
		o42a_val_gc_none);

enum number_signs {

	PLUS_SIGN = 0x002b,

	HYPHEN_MINUS = 0x002d,

	MINUS_SIGN = 0x2212

};

void o42a_int_by_str(
		o42a_val_t *const result,
		const o42a_val_t *const input,
		const uint32_t radix) {
	O42A_ENTER(return);

	if (!(input->flags & O42A_TRUE)) {
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	const size_t len = input->length;

	if (!len) {
		O42A(o42a_error_print(
				"Empty string can not be converted to integer"));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	o42a_bool_t space = O42A_FALSE;
	o42a_bool_t negative = O42A_FALSE;
	o42a_bool_t has_value = O42A_FALSE;
	int64_t value = 0;
	const size_t ashift = O42A(o42a_val_ashift(input));
	const UChar32 cmask = O42A(o42a_str_cmask(input));
	const char *const str = O42A(o42a_val_data(input));

	for (size_t i = 0; i < len; ++i) {

		const UChar32 c = *((UChar32*) (str + (i << ashift))) & cmask;

		if (!i) {
			switch (c) {
			case HYPHEN_MINUS:
			case MINUS_SIGN:
				negative = O42A_TRUE;
				continue;
			case PLUS_SIGN:
				continue;
			}
		} else if (u_charType(c) == U_SPACE_SEPARATOR) {
			if (space) {
				O42A(o42a_error_printf(
						"Two subsequent spaces in number at position %zu",
						i));
				result->flags = O42A_FALSE;
				O42A_RETURN;
			}
			space = O42A_TRUE;
			continue;
		}

		const int32_t digit = u_digit(c, radix);

		if (digit < 0) {
			O42A(o42a_error_printf(
					"Illegal character in number at position %zu",
					i));
			result->flags = O42A_FALSE;
			O42A_RETURN;
		}

		if (negative) {
			value = value * radix - digit;
			if (value > 0) {
				O42A(o42a_error_print("Integer overflow"));
				result->flags = O42A_FALSE;
				O42A_RETURN;
			}
		} else {
			value = value * radix + digit;
			if (value < 0) {
				O42A(o42a_error_print("Integer overflow"));
				result->flags = O42A_FALSE;
				O42A_RETURN;
			}
		}

		space = O42A_FALSE;
		has_value = O42A_TRUE;
	}

	if (space) {
		O42A(o42a_error_printf(
				"Unexpected space after number at position %zu",
				len - 1));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}
	if (!has_value) {
		O42A(o42a_error_print("Unexpected end of integer input"));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	result->value.v_integer = value;
	result->flags = O42A_TRUE;

	O42A_RETURN;
}

union str_and_int_ptr {
	const int64_t *p_integer;
	const char *p_char;
};

o42a_bool_t o42a_int_to_str(o42a_val_t *string, int64_t value) {
	O42A_ENTER(return O42A_TRUE);

	char buf[32];
	size_t len = O42A(snprintf(buf, 32, "%lld", (long long) value));

	if (len <= 8) {
		union str_and_int_ptr ptr = {.p_char = buf};
		string->flags = O42A_TRUE;
		string->length = len;
		string->value.v_integer = *ptr.p_integer;
		O42A_RETURN O42A_TRUE;
	}

	char *lbuf = O42A(o42a_refcount_alloc(len));

	if (!lbuf) {
		string->flags = O42A_FALSE;
		O42A_RETURN O42A_FALSE;
	}

	O42A(memcpy(lbuf, buf, len));
	string->flags = O42A_TRUE | O42A_VAL_EXTERNAL;
	string->length = len;
	string->value.v_ptr = lbuf;

	O42A_RETURN O42A_TRUE;
}
