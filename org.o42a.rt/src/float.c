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
#include "o42a/float.h"

#include "o42a/debug.h"
#include "o42a/error.h"
#include "o42a/string.h"

#include "unicode/uchar.h"

#include <fenv.h>
#include <math.h>


enum float_signs {

	PLUS_SIGN = 0x002b,

	COMMA = 0x002c,

	HYPHEN_MINUS = 0x002d,

	PERIOD = 0x002e,

	CAPITAL_E = 0x0045,

	SMALL_E = 0x0065,

	MINUS_SIGN = 0x2212

};


enum float_parse_stage {

	PARSE_SIGN = 0,
	PARSE_INT_MANTISSA = 1,
	PARSE_FRAC_MATISSA = 2,
	PARSE_EXPONENT_SIGN = 3,
	PARSE_EXPONENT = 4

};


void o42a_float_by_str(
		o42a_val_t *const result,
		const o42a_val_t *const input) {
	O42A_ENTER;

	if (!(input->flags & O42A_TRUE)) {
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	const size_t len = input->length;

	if (!len) {
		o42a_error_print("Empty string can not be converted to float");
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	fenv_t env;

	if (feholdexcept(&env)) {
		o42a_error_print("Internal error");
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}
	if (fesetround(FE_TONEAREST)) {
		o42a_error_print("Internal error");
		result->flags = O42A_FALSE;
		fesetenv(&env);
		O42A_RETURN;
	}

	double int_mantissa = 0.0;
	double frac_mantissa = 0.0;
	size_t frac_mantissa_len = 0;
	double exponent = 0.0;

	enum float_parse_stage stage = PARSE_SIGN;
	o42a_bool_t space = O42A_FALSE;
	o42a_bool_t negative = O42A_FALSE;
	double value = 0.0;
	const size_t step = o42a_val_alignment(input);
	const UChar32 cmask = o42a_str_cmask(input);
	const void *const str = o42a_val_data(input);

	for (size_t i = 0; i < len; i += step) {

		const UChar32 c = *((UChar32*) (str + i)) & cmask;

		switch (c) {
		case HYPHEN_MINUS:
		case MINUS_SIGN:
			if (stage != PARSE_SIGN && stage != PARSE_EXPONENT_SIGN) {
				break;
			}
			negative = O42A_TRUE;
			++stage;
			continue;
		case PLUS_SIGN:
			if (stage != PARSE_SIGN && stage != PARSE_EXPONENT_SIGN) {
				break;
			}
			++stage;
			continue;
		case COMMA:
		case PERIOD:
			if (stage > PARSE_INT_MANTISSA) {
				break;
			}
			if (space) {
				o42a_error_printf(
						"Unexpected space in number at position %zu",
						i);
				result->flags = O42A_FALSE;
				fesetenv(&env);
				O42A_RETURN;
			}
			int_mantissa = negative ? -value : value;
			space = O42A_FALSE;
			value = 0.0;
			stage = PARSE_FRAC_MATISSA;
			continue;
		case CAPITAL_E:
		case SMALL_E:
			if (stage >= PARSE_EXPONENT) {
				break;
			}
			if (space) {
				o42a_error_printf(
						"Unexpected space in number at position %zu",
						i);
				result->flags = O42A_FALSE;
				fesetenv(&env);
				O42A_RETURN;
			}
			if (stage == PARSE_FRAC_MATISSA) {
				frac_mantissa = negative ? -value : value;
			} else {
				int_mantissa = negative ? -value : value;
			}
			space = O42A_FALSE;
			value = 0.0;
			negative = O42A_FALSE;
			stage = PARSE_EXPONENT_SIGN;
			continue;
		default:
			if (u_charType(c) == U_SPACE_SEPARATOR) {
				if (space) {
					o42a_error_printf(
							"Two subsequent spaces in number at position %zu",
							i);
					result->flags = O42A_FALSE;
					fesetenv(&env);
					O42A_RETURN;
				}
				if (stage == PARSE_SIGN
						|| stage == PARSE_EXPONENT_SIGN
						|| (stage == PARSE_FRAC_MATISSA
								&& !frac_mantissa_len)) {
					o42a_error_printf(
							"Unexpected space in number at position %zu",
							i);
					result->flags = O42A_FALSE;
					fesetenv(&env);
					O42A_RETURN;
				}
				space = O42A_TRUE;
				continue;
			}
		}

		const int32_t digit = u_digit(c, 10);

		if (digit < 0) {
			o42a_error_printf(
					"Illegal character in number at position %zu",
					i);
			result->flags = O42A_FALSE;
			fesetenv(&env);
			O42A_RETURN;
		}

		value = fma(value, 10.0, digit);
		if (o42a_float_error(result)) {
			fesetenv(&env);
			O42A_RETURN;
		}
		if (stage == PARSE_FRAC_MATISSA) {
			++frac_mantissa_len;
		} else if (stage == PARSE_SIGN) {
			stage = PARSE_INT_MANTISSA;
		}

		space = O42A_FALSE;
	}

	if (space) {
		o42a_error_print("Unexpected space after number");
		result->flags = O42A_FALSE;
		fesetenv(&env);
		O42A_RETURN;
	}

	switch (stage) {
	case PARSE_INT_MANTISSA:
		int_mantissa = negative ? -value : value;
		break;
	case PARSE_FRAC_MATISSA:
		frac_mantissa = negative ? -value : value;
		break;
	case PARSE_EXPONENT:
		exponent = negative ? -value : value;
		break;
	default:
		o42a_error_print("Unexpected end of floating-point number input");
		result->flags = O42A_FALSE;
		fesetenv(&env);
		O42A_RETURN;
	}

	double res;

	if (!frac_mantissa_len) {
		res = int_mantissa;
	} else {
		res = fma(
				frac_mantissa,
				pow(10.0, -frac_mantissa_len),
				int_mantissa);
		if (o42a_float_error(result)) {
			fesetenv(&env);
			O42A_RETURN;
		}
	}
	if (stage == PARSE_EXPONENT) {
		res = fma(
				res,
				pow(10.0, exponent),
				0.0);
		if (o42a_float_error(result)) {
			fesetenv(&env);
			O42A_RETURN;
		}
	}

	result->flags = O42A_TRUE;
	result->value.v_float = res;
	fesetenv(&env);

	O42A_RETURN;
}

inline int o42a_float_error(o42a_val_t *const value) {

	const int error = fetestexcept(
			FE_DIVBYZERO | FE_INVALID | FE_UNDERFLOW | FE_OVERFLOW);

	if (!error) {
		return 0;
	}
	if (error & FE_UNDERFLOW) {
		o42a_error_print("Floating point number underflow");
	} else if (error & FE_OVERFLOW) {
		o42a_error_print("Floating point number overflow");
	} else if (error & FE_DIVBYZERO) {
		o42a_error_print("Division by zero");
	} else {
		o42a_error_print("Floating point error");
	}

	value->flags = O42A_FALSE;

	return error;
}
