/*
    Copyright (C) 2011-2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/type/float.h"

#include <fenv.h>
#include <math.h>
#include <stdio.h>
#include <string.h>

#include "o42a/error.h"
#include "o42a/memory/refcount.h"
#include "o42a/type/string.h"

#include "unicode/uchar.h"


const o42a_val_type_t o42a_val_type_float = O42A_VAL_TYPE(
		"float",
		o42a_val_gc_none,
		o42a_val_gc_none);

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
	O42A_ENTER(return);

	if (!(input->flags & O42A_TRUE)) {
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	const size_t len = input->length;

	if (!len) {
		O42A(o42a_error_print("Empty string can not be converted to float"));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}

	fenv_t env;

	if (feholdexcept(&env)) {
		O42A(o42a_error_print("Internal error"));
		result->flags = O42A_FALSE;
		O42A_RETURN;
	}
	if (fesetround(FE_TONEAREST)) {
		O42A(o42a_error_print("Internal error"));
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
	const size_t ashift = O42A(o42a_val_ashift(input));
	const UChar32 cmask = O42A(o42a_str_cmask(input));
	const char *const str = O42A(o42a_val_data(input));

	for (size_t i = 0; i < len; ++i) {

		const UChar32 c = *((UChar32*) (str + (i << ashift))) & cmask;

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
				O42A(o42a_error_printf(
						"Unexpected space in number at position %zu",
						i));
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
				O42A(o42a_error_printf(
						"Unexpected space in number at position %zu",
						i));
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
			if (u_charType(c) != U_SPACE_SEPARATOR) {
				break;
			}
			if (space) {
				O42A(o42a_error_printf(
						"Two subsequent spaces in number at position %zu",
						i));
				result->flags = O42A_FALSE;
				fesetenv(&env);
				O42A_RETURN;
			}
			if (stage == PARSE_SIGN
					|| stage == PARSE_EXPONENT_SIGN
					|| (stage == PARSE_FRAC_MATISSA
							&& !frac_mantissa_len)) {
				O42A(o42a_error_printf(
						"Unexpected space in number at position %zu",
						i));
				result->flags = O42A_FALSE;
				fesetenv(&env);
				O42A_RETURN;
			}
			space = O42A_TRUE;
			continue;
		}

		const int32_t digit = u_digit(c, 10);

		if (digit < 0) {
			O42A(o42a_error_printf(
					"Illegal character in number at position %zu",
					i));
			result->flags = O42A_FALSE;
			fesetenv(&env);
			O42A_RETURN;
		}

		value = fma(value, 10.0, digit);
		if (O42A(o42a_float_error(result))) {
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
		O42A(o42a_error_print("Unexpected space after number"));
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
		O42A(o42a_error_print(
				"Unexpected end of floating-point number input"));
		result->flags = O42A_FALSE;
		fesetenv(&env);
		O42A_RETURN;
	}

	double res;

	if (!frac_mantissa_len) {
		res = int_mantissa;
	} else {
		res = fma(frac_mantissa, pow(0.1, frac_mantissa_len), int_mantissa);
		if (O42A(o42a_float_error(result))) {
			fesetenv(&env);
			O42A_RETURN;
		}
	}
	if (stage == PARSE_EXPONENT) {
		res = fma(res, pow(10.0, exponent), 0.0);
		if (O42A(o42a_float_error(result))) {
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
	O42A_ENTER(return 0);

	const int error = fetestexcept(
			FE_DIVBYZERO | FE_INVALID | FE_UNDERFLOW | FE_OVERFLOW);

	if (!error) {
		O42A_RETURN 0;
	}
	if (error & FE_UNDERFLOW) {
		O42A(o42a_error_print("Floating point number underflow"));
	} else if (error & FE_OVERFLOW) {
		O42A(o42a_error_print("Floating point number overflow"));
	} else if (error & FE_DIVBYZERO) {
		O42A(o42a_error_print("Division by zero"));
	} else {
		O42A(o42a_error_print("Floating point error"));
	}

	value->flags = O42A_FALSE;

	O42A_RETURN error;
}

union str_and_int_ptr {
	const int64_t *p_integer;
	const char *p_char;
};

static const char *const O42A_NAN = "NaN";
static const char *const O42A_ZERO = "0.0";
static const char *const O42A_POSINF = "Infinity";
static const char *const O42A_NEGINF = "-Infinity";

o42a_bool_t o42a_float_to_str(
		o42a_val_t *const string,
		double value) {
	O42A_ENTER(return O42A_TRUE);

	if (isinf(value)) {
		if (!signbit(value)) {
			union str_and_int_ptr ptr = {.p_char = O42A_POSINF};
			string->flags = O42A_TRUE;
			string->length = 8;
			string->value.v_integer = *ptr.p_integer;
		} else {
			string->flags = O42A_TRUE | O42A_VAL_EXTERNAL | O42A_VAL_STATIC;
			string->length = 9;
			string->value.v_ptr = (void *) O42A_NEGINF;
		}
		O42A_RETURN O42A_TRUE;
	}

	if (isnan(value)) {
		union str_and_int_ptr ptr = {.p_char = O42A_NAN};
		string->flags = O42A_TRUE;
		string->length = 3;
		string->value.v_integer = *ptr.p_integer;
		O42A_RETURN O42A_TRUE;
	}

	double absval = fabs(value);

	if (absval == 0.0) {
		union str_and_int_ptr ptr = {.p_char = O42A_ZERO};
		string->flags = O42A_TRUE;
		string->length = 3;
		string->value.v_integer = *ptr.p_integer;
		O42A_RETURN O42A_TRUE;
	}

	char buf[32];
	const char *format;
	int rmzeros;

	if (absval >= 0.001 && absval < 1000000) {
		format = "%#.3f";
		rmzeros = 1;
	} else {
		format = "%#.6e";
		rmzeros = 0;
	}

	size_t len = O42A(snprintf(buf, 32, format, value));

	if (rmzeros) {
		size_t i = len;
		while (i) {
			char c = buf[--i];
			if (c != '0') {
				if (c == '.' && i < len - 1) {
					len = i + 2;
				} else {
					len = i + 1;
				}
				break;
			}
		}
	}

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
