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

#include "o42a/debug.h"
#include "o42a/string.h"

#include <stdarg.h>
#include <stdio.h>
#include <wchar.h>


void o42a_error_print_str(const o42a_val_t *message) {
	O42A_ENTER;

	o42a_error_start();
	o42a_error_append_str(message);
	o42a_error_end();

	O42A_RETURN;
}

void o42a_error_print(const wchar_t *message) {
	O42A_ENTER;

	o42a_error_start();
	fputws(message, stderr);
	o42a_error_end();

	O42A_RETURN;
}

void o42a_error_printf(const wchar_t *format, ...) {
	O42A_ENTER;

	va_list args;

	va_start(args, format);
	o42a_error_start();
	vfwprintf(stderr, format, args);
	va_end(args);
	o42a_error_end();

	O42A_RETURN;
}


inline void o42a_error_start() {
	O42A_ENTER;

	fputws(L"[ERROR] ", stderr);

	O42A_RETURN;
}

void o42a_error_append_str(const o42a_val_t *message) {
	O42A_ENTER;

	const size_t len = message->length;

	if (!len) {
		O42A_RETURN;
	}

	const size_t step = o42a_val_alignment(message);
	const size_t mask = o42a_str_wchar_mask(message);
	const void *const str = o42a_val_data(message);

	for (size_t i = 0; i < len; i += step) {
		putwc(*((wchar_t*) (str + i)) & mask, stderr);
	}

	O42A_RETURN;
}

inline void o42a_error_append(const wchar_t *message) {
	O42A_ENTER;

	fputws(message, stderr);

	O42A_RETURN;
}

void o42a_error_appendf(const wchar_t *format, ...) {
	O42A_ENTER;

	va_list args;

	va_start(args, format);
	vfwprintf(stderr, format, args);
	va_end(args);

	O42A_RETURN;
}

inline void o42a_error_end() {
	O42A_ENTER;

	fputwc(L'\n', stderr);

	O42A_RETURN;
}
