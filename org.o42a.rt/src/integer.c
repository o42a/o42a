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

#include "o42a/error.h"
#include "o42a/string.h"

#include "unicode/uchar.h"


enum number_signs {

	PLUS_SIGN = 0x002b,

	HYPHEN_MINUS = 0x002d,

	MINUS_SIGN = 0x2212

};

void o42a_int_by_str(
		O42A_PARAMS
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
				O42A_ARGS
				"Empty string can not be converted to integer"));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	o42a_bool_t space = O42A_FALSE;
	o42a_bool_t negative = O42A_FALSE;
	o42a_bool_t has_value = O42A_FALSE;
	int64_t value = 0;
	const size_t step = O42A(o42a_val_alignment(O42A_ARGS input));
	const UChar32 cmask = O42A(o42a_str_cmask(O42A_ARGS input));
	const void *const str = O42A(o42a_val_data(O42A_ARGS input));

	for (size_t i = 0; i < len; i += step) {

		const UChar32 c = *((UChar32*) (str + i)) & cmask;

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
						O42A_ARGS
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
					O42A_ARGS
					"Illegal character in number at position %zu",
					i));
			result->flags = O42A_FALSE;
			O42A_RETURN;
		}

		if (negative) {
			value = value * radix - digit;
			if (value > 0) {
				O42A(o42a_error_print(O42A_ARGS "Integer overflow"));
				result->flags = O42A_FALSE;
				O42A_RETURN;
			}
		} else {
			value = value * radix + digit;
			if (value < 0) {
				O42A(o42a_error_print(O42A_ARGS "Integer overflow"));
				result->flags = O42A_FALSE;
				O42A_RETURN;
			}
		}

		space = O42A_FALSE;
		has_value = O42A_TRUE;
	}

	if (space) {
		O42A(o42a_error_printf(
				O42A_ARGS
				"Unexpected space after number at position %zu",
				len - 1));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}
	if (!has_value) {
		O42A(o42a_error_print(O42A_ARGS "Unexpected end of integer input"));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	result->value.v_integer = value;
	result->flags = O42A_TRUE;

	O42A_RETURN;
}
