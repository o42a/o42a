/*
    Copyright (C) 2011-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/error.h"

#include "o42a/type/string.h"

#include "unicode/ustdio.h"


extern void o42a_error_start();

extern void o42a_error_end();

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

extern void o42a_error_append(const char *);

void o42a_error_appendf(const char *const format, ...) {

	va_list args;

	va_start(args, format);
	vfprintf(stderr, format, args);
	va_end(args);
}
