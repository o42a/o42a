/*
    Run-Time Library
    Copyright (C) 2011,2012 Ruslan Lopatin

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

#include "o42a/type/string.h"

#include "unicode/ustdio.h"


void o42a_error_print_str(const o42a_val_t *const message) {
	o42a_error_start();
	o42a_error_append_str(message);
	o42a_error_end();
}

void o42a_error_print(const char *const message) {
	o42a_error_start();
	fputs(message, stderr);
	o42a_error_end();
}

void o42a_error_printf(const char *const format, ...) {

	va_list args;

	va_start(args, format);
	o42a_error_start();
	vfprintf(stderr, format, args);
	va_end(args);
	o42a_error_end();
}


inline void o42a_error_start() {
	fputs("[E] ", stderr);
}

void o42a_error_append_str(const o42a_val_t *const message) {

	const size_t len = message->length;

	if (!len) {
		return;
	}

	const size_t ashift = o42a_val_ashift(message);
	const UChar32 cmask = o42a_str_cmask(message);
	const char *const str = o42a_val_data(message);

	UFILE *const uerr = u_finit(stderr, NULL, NULL);

	for (size_t i = 0; i < len; ++i) {
		u_fputc(*((UChar32*) (str + (i << ashift))) & cmask, uerr);
	}

	u_fclose(uerr);
}

inline void o42a_error_append(const char *const message) {
	fputs(message, stderr);
}

void o42a_error_appendf(const char *const format, ...) {

	va_list args;

	va_start(args, format);
	vfprintf(stderr, format, args);
	va_end(args);
}

inline void o42a_error_end() {
	fputc('\n', stderr);
}
