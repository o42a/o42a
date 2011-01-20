/*
    Run-Time Library
    Copyright (C) 2011 Ruslan Lopatin

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
#include "o42a/integer.h"

#include "o42a/debug.h"
#include "o42a/error.h"
#include "o42a/string.h"

#include "unicode/uchar.h"


enum number_signs {

	PLUS_SIGN = 0x002b,

	HYPHEN_MINUS = 0x002d,

	MINUS_SIGN = 0x2212

};

void o42a_int_by_str(
		o42a_val_t *const result,
		const o42a_val_t *const string,
		const uint8_t radix) {
	O42A_ENTER;

	if (!(string->flags & O42A_TRUE)) {
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	const size_t len = string->length;

	if (!len) {
		o42a_error_print("Empty string can not be converted to integer");
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	o42a_bool_t space = O42A_FALSE;
	o42a_bool_t negative = O42A_FALSE;
	o42a_bool_t has_value = O42A_FALSE;
	uint64_t max = INT64_MAX;
	uint64_t value = 0;
	const size_t step = o42a_val_alignment(string);
	const UChar32 cmask = o42a_str_cmask(string);
	const void *const str = o42a_val_data(string);

	for (size_t i = 0; i < len; i += step) {

		const UChar32 c = *((UChar32*) (str + i)) & cmask;

		if (!i) {
			switch (c) {
			case HYPHEN_MINUS:
			case MINUS_SIGN:
				negative = O42A_TRUE;
				max = ((uint64_t) INT64_MAX) + 1;
				continue;
			case PLUS_SIGN:
				continue;
			}
		} else if (u_charType(c) == U_SPACE_SEPARATOR) {
			if (space) {
				o42a_error_print("Two subsequent spaces in number");
				result->flags = O42A_FALSE;
				O42A_RETURN;
			}
			space = O42A_TRUE;
			continue;
		}

		const uint32_t digit = u_digit(c, radix);

		if (!digit) {
			o42a_error_printf("Illegal character in number at position %zu", i);
			result->flags = O42A_FALSE;
			O42A_RETURN;
		}

		value = value * radix + digit;

		if (value > max) {
			o42a_error_print("Integer overflow");
			result->flags = O42A_FALSE;
			O42A_RETURN;
		}

		space = O42A_FALSE;
		has_value = O42A_TRUE;
	}

	if (space) {
		o42a_error_print("Unexpected space after number");
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}
	if (!has_value) {
		o42a_error_print("Unexpected end of integer input");
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	if (!negative) {
		result->value.v_integer = value;
	} else {
		result->value.v_integer = -((int64_t) value);
	}

	result->flags = O42A_TRUE;

	O42A_RETURN;
}
