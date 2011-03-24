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
#include "o42a/error.h"

#include "o42a/string.h"

#include "unicode/ustdio.h"


void o42a_error_print_str(O42A_PARAMS const o42a_val_t *const message) {
	O42A_ENTER(return);

	O42A(o42a_error_start(O42A_ARG));
	O42A(o42a_error_append_str(O42A_ARGS message));
	O42A(o42a_error_end(O42A_ARG));

	O42A_RETURN;
}

void o42a_error_print(O42A_PARAMS const char *const message) {
	O42A_ENTER(return);

	O42A(o42a_error_start(O42A_ARG));
	O42A(fputs(message, stderr));
	O42A(o42a_error_end(O42A_ARG));

	O42A_RETURN;
}

void o42a_error_printf(O42A_PARAMS const char *const format, ...) {
	O42A_ENTER(return);

	va_list args;

	va_start(args, format);
	O42A(o42a_error_start(O42A_ARG));

	O42A(vfprintf(stderr, format, args));

	va_end(args);
	O42A(o42a_error_end(O42A_ARG));

	O42A_RETURN;
}


inline void o42a_error_start(O42A_PARAM) {
	O42A_ENTER(return);

	O42A(fputs("[E] ", stderr));

	O42A_RETURN;
}

void o42a_error_append_str(O42A_PARAMS const o42a_val_t *const message) {
	O42A_ENTER(return);

	const size_t len = message->length;

	if (!len) {
		O42A_RETURN;
	}

	const size_t step = O42A(o42a_val_alignment(O42A_ARGS message));
	const UChar32 cmask = O42A(o42a_str_cmask(O42A_ARGS message));
	const void *const str = O42A(o42a_val_data(O42A_ARGS message));

	UFILE *const uerr = O42A(u_finit(stderr, NULL, NULL));

	for (size_t i = 0; i < len; i += step) {
		O42A(u_fputc(*((UChar32*) (str + i)) & cmask, uerr));
	}

	O42A(u_fclose(uerr));

	O42A_RETURN;
}

inline void o42a_error_append(O42A_PARAMS const char *const message) {
	O42A_ENTER(return);

	O42A(fputs(message, stderr));

	O42A_RETURN;
}

void o42a_error_appendf(O42A_PARAMS const char *const format, ...) {
	O42A_ENTER(return);

	va_list args;

	va_start(args, format);
	O42A(vfprintf(stderr, format, args));
	va_end(args);

	O42A_RETURN;
}

inline void o42a_error_end(O42A_PARAM) {
	O42A_ENTER(return);

	O42A(fputc('\n', stderr));

	O42A_RETURN;
}
